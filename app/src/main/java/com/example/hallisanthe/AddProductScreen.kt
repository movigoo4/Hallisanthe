package com.example.hallisanthe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hallisanthe.ui.theme.CreamWhite
import com.example.hallisanthe.ui.theme.IndiaGreen
import com.example.hallisanthe.ui.theme.Saffron
import com.example.hallisanthe.ui.theme.SaffronDark
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController) {

    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var sellerName by remember { mutableStateOf("") }
    var sellerLocation by remember { mutableStateOf("") }
    var sellerPhone by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val categories = listOf("Toys", "Textiles", "Food", "Handicrafts", "Jewellery", "Art")

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    fun uploadImageToCloudinary(uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "upload_image.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val client = OkHttpClient()
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file", file.name,
                        file.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                    .addFormDataPart("upload_preset", "halli_santhe_preset")
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/dnxf3dqia/image/upload")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "")
                val imageUrl = json.getString("secure_url")

                withContext(Dispatchers.Main) {
                    onSuccess(imageUrl)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Upload failed")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(colors = listOf(Saffron, SaffronDark))
                    )
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CreamWhite)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Add Product", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CreamWhite)
                        Text("List your product on Halli Santhe", fontSize = 12.sp, color = CreamWhite.copy(alpha = 0.85f))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text("Product Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SaffronDark)

            // Image Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Saffron.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap to select image from gallery",
                            fontSize = 14.sp,
                            color = Saffron,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    unfocusedBorderColor = Saffron.copy(alpha = 0.4f),
                    focusedLabelColor = Saffron
                )
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (Rs.)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    unfocusedBorderColor = Saffron.copy(alpha = 0.4f),
                    focusedLabelColor = Saffron
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    unfocusedBorderColor = Saffron.copy(alpha = 0.4f),
                    focusedLabelColor = Saffron
                )
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Saffron,
                        unfocusedBorderColor = Saffron.copy(alpha = 0.4f),
                        focusedLabelColor = Saffron
                    )
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = { selectedCategory = category; expanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Seller Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SaffronDark)

            OutlinedTextField(
                value = sellerName,
                onValueChange = { sellerName = it },
                label = { Text("Seller Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndiaGreen,
                    unfocusedBorderColor = IndiaGreen.copy(alpha = 0.4f),
                    focusedLabelColor = IndiaGreen
                )
            )

            OutlinedTextField(
                value = sellerLocation,
                onValueChange = { sellerLocation = it },
                label = { Text("Seller Location") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndiaGreen,
                    unfocusedBorderColor = IndiaGreen.copy(alpha = 0.4f),
                    focusedLabelColor = IndiaGreen
                )
            )

            OutlinedTextField(
                value = sellerPhone,
                onValueChange = { sellerPhone = it },
                label = { Text("Seller Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndiaGreen,
                    unfocusedBorderColor = IndiaGreen.copy(alpha = 0.4f),
                    focusedLabelColor = IndiaGreen
                )
            )

            if (message.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("success", ignoreCase = true))
                            IndiaGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = if (message.contains("success", ignoreCase = true))
                            IndiaGreen else MaterialTheme.colorScheme.error
                    )
                }
            }

            Button(
                onClick = {
                    if (name.isEmpty() || price.isEmpty()) {
                        message = "Please fill name and price!"
                        return@Button
                    }
                    if (selectedImageUri == null) {
                        message = "Please select an image!"
                        return@Button
                    }
                    isLoading = true
                    message = "Uploading image..."

                    uploadImageToCloudinary(
                        uri = selectedImageUri!!,
                        onSuccess = { imageUrl ->
                            val db = FirebaseFirestore.getInstance()
                            val product = hashMapOf(
                                "name" to name,
                                "price" to price,
                                "description" to description,
                                "imageUrl" to imageUrl,
                                "sellerName" to sellerName,
                                "sellerLocation" to sellerLocation,
                                "sellerPhone" to sellerPhone,
                                "category" to selectedCategory
                            )
                            db.collection("products")
                                .add(product)
                                .addOnSuccessListener {
                                    isLoading = false
                                    message = "Product added successfully!"
                                    name = ""; price = ""; description = ""
                                    selectedImageUri = null
                                    sellerName = ""; sellerLocation = ""
                                    sellerPhone = ""; selectedCategory = ""
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    message = "Error: ${it.message}"
                                }
                        },
                        onError = { error ->
                            isLoading = false
                            message = "Image upload failed: $error"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isLoading) "Please wait..." else "Add Product",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CreamWhite
                )
            }
        }
    }
}