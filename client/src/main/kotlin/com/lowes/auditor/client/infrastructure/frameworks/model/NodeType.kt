package com.lowes.auditor.client.infrastructure.frameworks.model

/**
 * List out different types of node.
 * TEXT: means it a mode containing text value
 * OBJECT: means it a mode containing object/nested values
 * ARRAY: means it a mode that is iterable.
 */
enum class NodeType {
    TEXT,
    OBJECT,
    ARRAY
}
