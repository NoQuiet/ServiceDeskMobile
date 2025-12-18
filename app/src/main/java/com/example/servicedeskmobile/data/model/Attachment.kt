package com.example.servicedeskmobile.data.model

import com.google.gson.annotations.SerializedName

data class Attachment(
    val id: Int,
    @SerializedName("file_name")
    val fileName: String,
    @SerializedName("file_path")
    val filePath: String,
    @SerializedName("file_size")
    val fileSize: Int,
    @SerializedName("mime_type")
    val mimeType: String,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class AttachmentUploadResponse(
    val message: String,
    val attachments: List<Attachment>
)
