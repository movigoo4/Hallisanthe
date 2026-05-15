package com.example.hallisanthe

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hallisanthe.ui.theme.CreamWhite
import com.example.hallisanthe.ui.theme.IndiaGreen
import com.example.hallisanthe.ui.theme.Saffron
import com.example.hallisanthe.ui.theme.SaffronDark
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: String, navController: NavController) {

    var product by remember { mutableStateOf<Product?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(productId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("products").document(productId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null) {
                    product = Product(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        price = doc.getString("price") ?: "",
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        sellerName = doc.getString("sellerName") ?: "",
                        sellerLocation = doc.getString("sellerLocation") ?: "",
                        sellerPhone = doc.getString("sellerPhone") ?: ""
                    )
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Saffron, SaffronDark)
                        )
                    )
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = CreamWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product?.name ?: "Product Detail",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CreamWhite
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Saffron)
            }
        } else if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Product not found!", color = Saffron)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = product!!.imageUrl,
                    contentDescription = product!!.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product!!.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .background(Saffron, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "₹${product!!.price}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CreamWhite
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CreamWhite),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "📝 Description",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SaffronDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = product!!.description,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = IndiaGreen.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🧑‍🌾 Seller Info",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = IndiaGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "👤 ${product!!.sellerName}", fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "📍 ${product!!.sellerLocation}", fontSize = 14.sp)
                            if (product!!.sellerPhone.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "📞 ${product!!.sellerPhone}", fontSize = 14.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val phone = product!!.sellerPhone.trim()
                            val msg = "Hi! I'm interested in your product: ${product!!.name} listed on Halli Santhe for ₹${product!!.price}"
                            val url = "https://wa.me/91$phone?text=${Uri.encode(msg)}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndiaGreen),
                        shape = RoundedCornerShape(12.dp),
                        enabled = product!!.sellerPhone.isNotEmpty()
                    ) {
                        Text(
                            text = "💬 Contact on WhatsApp",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CreamWhite
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:${product!!.sellerPhone}")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = product!!.sellerPhone.isNotEmpty()
                    ) {
                        Text(
                            text = "📞 Call Seller",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Saffron
                        )
                    }
                }
            }
        }
    }
}