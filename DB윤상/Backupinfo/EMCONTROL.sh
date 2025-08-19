#!/bin/bash

PDB_NAME=$1
ACTION=$2

case "$PDB_NAME" in
  CDB)
    CONTAINER="CDB$ROOT"
    ;;
  STORE)
    CONTAINER="PDB_STORE"
    ;;
  BOOKING)
    CONTAINER="PDB_BOOKING"
    ;;
  USER)
    CONTAINER="PDB_USER"
    ;;
  *)
    echo "Unknown PDB: $PDB_NAME"
    exit 1
    ;;
esac

case "$ACTION" in
  start)
    PORT=5500
    ;;
  stop)
    PORT=0
    ;;
  *)
    echo "Unknown action: $ACTION"
    exit 1
    ;;
esac

sqlplus / as sysdba <<EOF
ALTER SESSION SET CONTAINER=$CONTAINER;
BEGIN
  DBMS_XDB_CONFIG.SETHTTPSPORT($PORT);
END;
/
EXIT;
EOF

