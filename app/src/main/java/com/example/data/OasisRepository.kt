package com.example.data

import kotlinx.coroutines.flow.Flow

class OasisRepository(private val oasisDao: OasisDao) {
    val allMantras: Flow<List<MantraEntity>> = oasisDao.getAllMantras()
    val allScenarios: Flow<List<OasisScenarioEntity>> = oasisDao.getAllScenarios()
    val allSessions: Flow<List<SessionEntity>> = oasisDao.getAllSessions()

    suspend fun insertMantra(mantra: MantraEntity) {
        oasisDao.insertMantra(mantra)
    }

    suspend fun deleteMantra(mantra: MantraEntity) {
        oasisDao.deleteMantra(mantra)
    }

    suspend fun insertScenario(scenario: OasisScenarioEntity) {
        oasisDao.insertScenario(scenario)
    }

    suspend fun deleteScenario(scenario: OasisScenarioEntity) {
        oasisDao.deleteScenario(scenario)
    }

    suspend fun insertSession(session: SessionEntity) {
        oasisDao.insertSession(session)
    }

    suspend fun clearSessions() {
        oasisDao.clearSessions()
    }
}
