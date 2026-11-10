package com.zaidashiq.ultra.repositories

import com.zaidashiq.ultra.database.ActivityLogEntity
import com.zaidashiq.ultra.database.AutomationRuleEntity
import com.zaidashiq.ultra.database.UltraDao
import kotlinx.coroutines.flow.Flow

class AutomationRuleRepository(
    private val ultraDao: UltraDao
) {
    val allMessageRules: Flow<List<AutomationRuleEntity>> = ultraDao.observeAutomationRules()
    val activeMessageRules: Flow<List<AutomationRuleEntity>> = ultraDao.observeActiveAutomationRules()

    suspend fun getActiveRulesSnapshot(): List<AutomationRuleEntity> {
        return ultraDao.getEnabledAutomationRules()
    }

    suspend fun upsertMessageRule(rule: AutomationRuleEntity): Long {
        val id = ultraDao.upsertAutomationRule(rule)
        ultraDao.insertActivityLog(
            ActivityLogEntity(
                level = "INFO",
                module = "MessageRuleRepository",
                details = "Saved message automation rule '${rule.title}' (trigger: '${rule.triggerKeyword}', mode: ${rule.matchMode})"
            )
        )
        return id
    }

    suspend fun setRuleEnabled(ruleId: Long, enabled: Boolean) {
        ultraDao.setAutomationRuleEnabled(ruleId, enabled)
    }

    suspend fun recordRuleTriggered(ruleId: Long) {
        ultraDao.incrementRuleHitCount(ruleId)
    }

    suspend fun deleteRule(ruleId: Long) {
        ultraDao.deleteAutomationRule(ruleId)
    }
}
