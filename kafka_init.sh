#!/bin/sh
# Bootstrap de tópicos Kafka (dev/prod) – idempotente, sem set -e
# - Espera brokers ficarem prontos
# - Deleta (se existir) e espera sumir
# - Cria com RF/ISR e partições desejadas
# - Descreve ao final

# --------- PARÂMETROS (pode sobrescrever via environment) ----------
BOOTSTRAP="${BOOTSTRAP:-kafka-1:29092,kafka-2:29093,kafka-3:29094}"

# Mantém os nomes atuais para não quebrar seu código.
# (Se quiser evitar mistura de '.' e '_' em prod, troque por ex.:
#   TOPIC_MAIN="outbox-event-stock-updated"
#   TOPIC_DLQ="outbox-event-stock-updated-dlq"
# e ajuste producers/consumers.)
TOPIC_MAIN="${TOPIC_MAIN:-outbox_event.stock-updated}"
TOPIC_DLQ="${TOPIC_DLQ:-outbox_event.stock-updated-dlq}"

PARTITIONS_MAIN="${PARTITIONS_MAIN:-6}"
PARTITIONS_DLQ="${PARTITIONS_DLQ:-1}"

RETENTION_MS_MAIN="${RETENTION_MS_MAIN:-604800000}"  # 7 dias
RETENTION_MS_DLQ="${RETENTION_MS_DLQ:-604800000}"

RF_DESIRED="${RF:-3}"
MIN_ISR_DESIRED="${MIN_ISR:-2}"

MAX_TRIES="${MAX_TRIES:-30}"
SLEEP_SECS="${SLEEP_SECS:-2}"

log() { echo "[$(date +'%H:%M:%S')] $*"; }
die() { echo "ERRO: $*" >&2; exit 1; }

# --------- FUNÇÕES AUXILIARES ----------
brokers_count_from_bootstrap() {
  echo "$BOOTSTRAP" | tr ',' '\n' | grep -c .
}

validate_rf_isr() {
  BROKERS="$(brokers_count_from_bootstrap)"
  RF="$RF_DESIRED"
  MIN_ISR="$MIN_ISR_DESIRED"

  if [ "$RF" -gt "$BROKERS" ]; then
    log "WARN: RF ($RF) > brokers do BOOTSTRAP ($BROKERS). Ajustando RF=$BROKERS."
    RF="$BROKERS"
  fi
  if [ "$MIN_ISR" -gt "$RF" ]; then
    log "WARN: min.insync.replicas ($MIN_ISR) > RF ($RF). Ajustando MIN_ISR=$RF."
    MIN_ISR="$RF"
  fi
  export RF MIN_ISR
}

wait_kafka_ready() {
  i=1
  while [ "$i" -le "$MAX_TRIES" ]; do
    if kafka-topics --bootstrap-server "$BOOTSTRAP" --list >/dev/null 2>&1; then
      log "Kafka respondeu em $i tentativa(s)."
      return 0
    fi
    log "Aguardando Kafka ($i/$MAX_TRIES)..."
    sleep "$SLEEP_SECS"
    i=$((i+1))
  done
  return 1
}

wait_brokers_up() {
  EXPECT="$(brokers_count_from_bootstrap)"
  j=1
  while [ "$j" -le "$MAX_TRIES" ]; do
    HAVE=$(kafka-broker-api-versions --bootstrap-server "$BOOTSTRAP" 2>/dev/null \
            | grep -Eo 'id: [0-9]+' | wc -l | tr -d ' ')
    if [ "$HAVE" -ge "$EXPECT" ]; then
      log "Brokers ativos: $HAVE/$EXPECT."
      return 0
    fi
    log "Aguardando brokers ($HAVE/$EXPECT) - tentativa $j/$MAX_TRIES..."
    sleep "$SLEEP_SECS"
    j=$((j+1))
  done
  return 1
}

topic_exists() {
  NAME="$1"
  kafka-topics --bootstrap-server "$BOOTSTRAP" --list 2>/dev/null | grep -qx "$NAME"
}

topic_delete_if_exists() {
  NAME="$1"
  kafka-topics --bootstrap-server "$BOOTSTRAP" --delete --if-exists --topic "$NAME" >/dev/null 2>&1 || true
}

wait_topic_gone() {
  NAME="$1"
  k=1
  while [ "$k" -le "$MAX_TRIES" ]; do
    if ! topic_exists "$NAME"; then
      return 0
    fi
    log "Aguardando deleção de '$NAME' ($k/$MAX_TRIES)..."
    sleep "$SLEEP_SECS"
    k=$((k+1))
  done
  return 1
}

topic_create_if_not_exists() {
  NAME="$1"; PARTS="$2"; RET_MS="$3"; EXTRA_CFG="$4"
  if topic_exists "$NAME"; then
    log "Tópico '$NAME' já existe — pulando criação."
    return 0
  fi
  kafka-topics --bootstrap-server "$BOOTSTRAP" \
    --create --if-not-exists \
    --topic "$NAME" \
    --partitions "$PARTS" \
    --replication-factor "$RF" \
    --config "retention.ms=$RET_MS" \
    --config "min.insync.replicas=$MIN_ISR" \
    $EXTRA_CFG
}

topic_describe() {
  NAME="$1"
  kafka-topics --bootstrap-server "$BOOTSTRAP" --describe --topic "$NAME" || true
}

# --------- EXECUÇÃO ----------
log "BOOTSTRAP=$BOOTSTRAP"
validate_rf_isr
log "Usando RF=$RF, min.insync.replicas=$MIN_ISR"

wait_kafka_ready || die "Kafka não respondeu em ${MAX_TRIES} tentativas."
wait_brokers_up || die "Cluster não atingiu a contagem de brokers do BOOTSTRAP."

# Deleta e espera sumir (remova esta seção se não quiser limpar em cada run)
log "Removendo tópicos (se existirem)..."
topic_delete_if_exists "$TOPIC_MAIN"
topic_delete_if_exists "$TOPIC_DLQ"
wait_topic_gone "$TOPIC_MAIN" || die "Timeout aguardando deleção de $TOPIC_MAIN"
wait_topic_gone "$TOPIC_DLQ"   || die "Timeout aguardando deleção de $TOPIC_DLQ"

# Cria
log "Criando tópico principal: $TOPIC_MAIN (partitions=$PARTITIONS_MAIN, RF=$RF)"
topic_create_if_not_exists "$TOPIC_MAIN" "$PARTITIONS_MAIN" "$RETENTION_MS_MAIN" ""

log "Criando DLQ: $TOPIC_DLQ (partitions=$PARTITIONS_DLQ, RF=$RF)"
topic_create_if_not_exists "$TOPIC_DLQ" "$PARTITIONS_DLQ" "$RETENTION_MS_DLQ" \
  "--config cleanup.policy=delete"

# Descreve
log "Descrição dos tópicos:"
topic_describe "$TOPIC_MAIN"
topic_describe "$TOPIC_DLQ"

log "Concluído."
