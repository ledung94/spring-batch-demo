package com.example.reportservice.repository;

import com.example.reportservice.utils.DateTimeUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import java.time.LocalDateTime;

@Repository
public class MsgLogRepository {
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public MsgLogRepository(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
    }

    @Transactional
    public void cutOffData(int month) {
        // Create procedure
        String query = "CREATE OR REPLACE PROCEDURE process_cutoff_msg_logs (\n" +
                "    p_to_date IN VARCHAR2\n" +
                ")\n" +
                "IS\n" +
                "BEGIN\n" +
                "    -- Thực hiện các thao tác như insert, update, delete...\n" +
                "    INSERT INTO MSG_LOG_BAK (LOGID, REQ_RESP, REQUEST, RESPONSE, TRACE_ID, STATUS, LOG_TIME, CHANNEL, SERVICE_CODE, TEMP_TYPE, TEMP_TITLE, CONTENT, SOURCE, EXTEN_TYPE, DESCRIPTION)\n" +
                "    SELECT LOGID, REQ_RESP, REQUEST, RESPONSE, TRACE_ID, STATUS, LOG_TIME, CHANNEL, SERVICE_CODE, TEMP_TYPE, TEMP_TITLE, CONTENT, SOURCE, EXTEN_TYPE, DESCRIPTION\n" +
                "    FROM MSG_LOG\n" +
                "    WHERE LOG_TIME <= TO_TIMESTAMP(p_to_date, 'DD-MM-YYYY HH24:MI:SS.FF3');\n" +
                "\n" +
                "    DELETE FROM MSG_LOG\n" +
                "    WHERE LOG_TIME <= TO_TIMESTAMP(p_to_date, 'DD-MM-YYYY HH24:MI:SS.FF3');\n" +
                "\n" +
                "    COMMIT;\n" +
                "EXCEPTION\n" +
                "    WHEN OTHERS THEN\n" +
                "        ROLLBACK;\n" +
                "        RAISE;\n" +
                "END process_cutoff_msg_logs;";

//        createProcedure(query);
        try {
            // Call procedure
            LocalDateTime now = LocalDateTime.now();
            String toDate = DateTimeUtils.formatLocalDateTime(now.minusMonths(month), "dd-MM-yyyy") + " 23:59:59";
            StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("process_cutoff_msg_logs");

            // Register parameter
            storedProcedure.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
            System.out.println("toDate: " + toDate);
            storedProcedure.setParameter(1, toDate);

            // Execute procedure
            storedProcedure.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createProcedure(String query) {
        jdbcTemplate.execute(query);
    }
}
