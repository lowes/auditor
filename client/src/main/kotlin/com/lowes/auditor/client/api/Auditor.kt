package com.lowes.auditor.client.api

import com.lowes.auditor.client.entities.domain.AuditorEventConfig

interface Auditor {

    fun audit(oldObject: Any?, newObject: Any?)

    fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?)
}
