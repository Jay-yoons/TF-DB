CREATE OR REPLACE TRIGGER TRG_USERS_DELETE
AFTER DELETE ON USERS
FOR EACH ROW
DECLARE
    PRAGMA AUTONOMOUS_TRANSACTION;
    v_err_msg VARCHAR2(4000);
BEGIN
    BEGIN
        DELETE FROM FAV_STORE@PDB_STORES_LINK
        WHERE USER_ID = :OLD.USER_ID;
        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            v_err_msg := SQLERRM;
            INSERT INTO delete_fail_log(user_id, fail_msg, fail_time)
            VALUES (:OLD.USER_ID, 'FAV_STORE delete failed: ' || v_err_msg, SYSTIMESTAMP);
            COMMIT;
    END;
    BEGIN
        DELETE FROM BOOKING@PDB_BOOKING_LINK
        WHERE USER_ID = :OLD.USER_ID;
        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            v_err_msg := SQLERRM;
            INSERT INTO delete_fail_log(user_id, fail_msg, fail_time)
            VALUES (:OLD.USER_ID, 'BOOKING delete failed: ' || v_err_msg, SYSTIMESTAMP);
            COMMIT;
    END;
    BEGIN
        DELETE FROM REVIEW@PDB_STORES_LINK
        WHERE USER_ID = :OLD.USER_ID;
        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            v_err_msg := SQLERRM;
            INSERT INTO delete_fail_log(user_id, fail_msg, fail_time)
            VALUES (:OLD.USER_ID, 'REVIEW delete failed: ' || v_err_msg, SYSTIMESTAMP);
            COMMIT;
    END;
END;
/
