package com.crowdfunding_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures columns added for subscription/receipt features exist on PostgreSQL.
 * Hibernate ddl-auto=update does not always alter existing tables reliably.
 */
@Component
public class DatabaseSchemaPatcher implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaPatcher.class);

  private final JdbcTemplate jdbcTemplate;

  public DatabaseSchemaPatcher(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      patchUsersTable();
      patchPaymentTable();
      patchInvestmentsTable();
      log.info("Database schema patch completed.");
    } catch (Exception ex) {
      log.error("Database schema patch failed: {}", ex.getMessage(), ex);
    }
  }

  private void patchUsersTable() {
    exec(
        "ALTER TABLE users ADD COLUMN IF NOT EXISTS creator_membership_active BOOLEAN NOT NULL DEFAULT FALSE");
    exec(
        "ALTER TABLE users ADD COLUMN IF NOT EXISTS investor_membership_active BOOLEAN NOT NULL DEFAULT FALSE");
  }

  private void patchPaymentTable() {
    if (!tableExists("payment")) {
      log.warn("Table \"payment\" not found; skipping payment column patch.");
      return;
    }
    exec(
        "ALTER TABLE payment ADD COLUMN IF NOT EXISTS payment_type VARCHAR(32) NOT NULL DEFAULT 'INVESTMENT'");
    exec("ALTER TABLE payment ADD COLUMN IF NOT EXISTS receipt_number VARCHAR(64)");
  }

  private void patchInvestmentsTable() {
    if (!tableExists("investments")) {
      log.warn("Table \"investments\" not found; skipping investments column patch.");
      return;
    }
    exec("ALTER TABLE investments ADD COLUMN IF NOT EXISTS payment_id BIGINT");
  }

  private boolean tableExists(String tableName) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?",
            Integer.class,
            tableName);
    return count != null && count > 0;
  }

  private void exec(String sql) {
    jdbcTemplate.execute(sql);
    log.debug("Executed: {}", sql);
  }
}
