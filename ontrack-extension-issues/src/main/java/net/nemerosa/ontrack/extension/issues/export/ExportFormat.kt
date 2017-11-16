package net.nemerosa.ontrack.extension.issues.export

/**
 * Definition of an export format for the issues.
 */
class ExportFormat(
        val id: String,
        val name: String,
        val type: String
) {
    companion object {
        val TEXT = ExportFormat("text", "Text", "text/plain")
        val MARKDOWN = ExportFormat("markdown", "Markdown", "text/plain")
        val HTML = ExportFormat("html", "HTML", "text/html")
    }
}
