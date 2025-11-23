package de.tum.hack.jb.interhyp.challenge.ui.goal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyListingDto
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyType
import de.tum.hack.jb.interhyp.challenge.presentation.goal.GoalSelectionViewModel
import de.tum.hack.jb.interhyp.challenge.ui.util.byteArrayToImageBitmap
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.yield
import kotlinx.coroutines.withContext
import de.tum.hack.jb.interhyp.challenge.ui.util.byteArrayToImageBitmap
import de.tum.hack.jb.interhyp.challenge.ui.util.formatUrlForCrossDomain
import org.koin.compose.koinInject

@Composable
fun GoalSelectionScreen(
    location: String,
    size: Double,
    propertyType: PropertyType,
    onContinue: (String?) -> Unit,
    viewModel: GoalSelectionViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Search for properties when screen loads
    LaunchedEffect(location, size, propertyType) {
        viewModel.searchProperties(location, size, propertyType)
    }
    
    // When embedded in OnboardingScreen's scrollable Column, we can't use fillMaxSize
    // Use a fixed height or let it size naturally
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp) // Minimum height to ensure LazyColumn has space
            .padding(16.dp)
    ) {
        Text(
            text = "Select Your Dream Home",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Choose a property that matches your goals",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Show listing count
        if (uiState.listings.isNotEmpty() && !uiState.isLoading) {
            Text(
                text = "Found ${uiState.listings.size} properties",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        // Content area - use fixed height since we're in a scrollable parent
        Box(modifier = Modifier.height(500.dp).fillMaxWidth()) {
            when {
            uiState.isLoading -> {
                Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Loading properties...")
                    }
                }
            }
            
            uiState.errorMessage != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = uiState.errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = { viewModel.searchProperties(location, size, propertyType) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            uiState.listings.isEmpty() && !uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No properties found matching your criteria",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { viewModel.searchProperties(location, size, propertyType) }
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }
            
                uiState.listings.isNotEmpty() && !uiState.isLoading && uiState.errorMessage == null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // LazyColumn to take available space
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // Use items(items = ...) API - always add items (empty list is handled by LazyColumn)
                            items(
                                items = uiState.listings,
                                key = { it.id }
                            ) { listing ->
                                PropertyListingCard(
                                    listing = listing,
                                    isSelected = uiState.selectedListing?.id == listing.id,
                                    onSelect = { 
                                        viewModel.selectListing(listing) 
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                viewModel.saveGoal()
                                onContinue(uiState.selectedListing?.getMainImageUrl())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.selectedListing != null
                        ) {
                            Text(
                                if (uiState.selectedListing != null) {
                                    "Continue with Selected Property"
                                } else {
                                    "Select a Property to Continue"
                                }
                            )
                        }
                    }
                }
            
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Unexpected state: isLoading=${uiState.isLoading}, listings=${uiState.listings.size}, error=${uiState.errorMessage}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyListingCard(
    listing: PropertyListingDto,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val httpClient: HttpClient = koinInject()
    var imageBitmap by remember(listing.id) { mutableStateOf<ImageBitmap?>(null) }
    var imageLoadingError by remember(listing.id) { mutableStateOf<String?>(null) }
    val imageUrl = listing.getMainImageUrl()
    
    // Load image if URL is available
    LaunchedEffect(imageUrl) {
        if (imageUrl != null) {
            try {
                val safeUrl = formatUrlForCrossDomain(imageUrl)
                val imageBytes = httpClient.get(safeUrl) {
                    headers {
                        append(HttpHeaders.Accept, "image/*")
                    }
                }.body<ByteArray>()

                // Yield to allow UI updates before heavy decoding
                yield()

                imageBitmap = byteArrayToImageBitmap(imageBytes)
                imageLoadingError = null
            } catch (e: Exception) {
                // Image loading failed, keep null to show placeholder
                imageBitmap = null
                imageLoadingError = e.message
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 140.dp)
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Property Image
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when {
                    imageBitmap != null -> {
                        Image(
                            bitmap = imageBitmap!!,
                            contentDescription = listing.title ?: "Property image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    imageUrl != null -> {
                        // Loading or failed to load
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }
                    else -> {
                        // No image URL
                        Text(
                            text = "🏠",
                            fontSize = 40.sp
                        )
                    }
                }
            }
            
            // Property Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title
                if (!listing.title.isNullOrBlank()) {
                    Text(
                        text = listing.title!!,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
                
                // Location
                Text(
                    text = listing.getLocationDisplay(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Price - prominently displayed
                if (listing.buyingPrice != null) {
                    Text(
                        text = listing.getFormattedPrice(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // Size, rooms, and price per sqm
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (listing.squareMeter != null) {
                        Text(
                            text = listing.getFormattedSize(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    listing.rooms?.let {
                        Text(
                            text = listing.getFormattedRooms(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    listing.pricePerSqm?.let {
                        Text(
                            text = "${it.toInt()} €/m²",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Selection Indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

