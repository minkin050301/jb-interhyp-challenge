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
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyListingDto
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyType
import de.tum.hack.jb.interhyp.challenge.presentation.goal.GoalSelectionViewModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import org.koin.compose.koinInject

@Composable
fun GoalSelectionScreen(
    location: String,
    size: Double,
    propertyType: PropertyType,
    onContinue: () -> Unit,
    viewModel: GoalSelectionViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Debug: Log state changes
    LaunchedEffect(uiState) {
        println("DEBUG: GoalSelectionScreen - State changed: isLoading=${uiState.isLoading}, errorMessage=${uiState.errorMessage}, listings.size=${uiState.listings.size}")
    }
    
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
        
        // Debug: Show listing count and details
        if (uiState.listings.isNotEmpty() && !uiState.isLoading) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "Found ${uiState.listings.size} properties",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                // Debug: Show first listing details
                if (uiState.listings.isNotEmpty()) {
                    val first = uiState.listings.first()
                    Text(
                        text = "First: ${first.title ?: "No title"}, Price: ${first.buyingPrice}, Size: ${first.squareMeter}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
        
        // Content area - use fixed height since we're in a scrollable parent
        Box(modifier = Modifier.height(500.dp).fillMaxWidth()) {
            when {
                uiState.isLoading -> {
                    LaunchedEffect(Unit) {
                        println("DEBUG: GoalSelectionScreen - Showing loading state")
                    }
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
                LaunchedEffect(uiState.errorMessage) {
                    println("DEBUG: GoalSelectionScreen - Showing error state: ${uiState.errorMessage}")
                }
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
                LaunchedEffect(Unit) {
                    println("DEBUG: GoalSelectionScreen - Showing empty state")
                }
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
                    // Debug: Log listings
                    LaunchedEffect(uiState.listings.size) {
                        println("DEBUG: GoalSelectionScreen - Showing listings (${uiState.listings.size} items)")
                        println("DEBUG: Rendering ${uiState.listings.size} listings")
                        uiState.listings.forEachIndexed { index, listing ->
                            println("DEBUG: Listing $index - ID: ${listing.id}, Title: ${listing.title}, Price: ${listing.buyingPrice}, Size: ${listing.squareMeter}")
                        }
                    }
                    
                    // Debug logging outside LazyColumn
                    LaunchedEffect(uiState.listings.size) {
                        println("DEBUG: LazyColumn - About to compose ${uiState.listings.size} items")
                        println("DEBUG: LazyColumn - Listings list: ${uiState.listings.map { it.id }}")
                    }
                    
                    Column(modifier = Modifier.fillMaxSize()) {
                        // LazyColumn to take available space - add visible background for debugging
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) // Debug: visible background
                        ) {
                            // Debug: Add a visible test item
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Text(
                                        text = "TEST: LazyColumn is rendering! Found ${uiState.listings.size} properties. Scroll down to see them.",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            // Use items(items = ...) API - always add items (empty list is handled by LazyColumn)
                            items(
                                items = uiState.listings,
                                key = { it.id }
                            ) { listing ->
                                // Debug log when item is composed
                                println("DEBUG: LazyColumn - Composing item for listing: ${listing.id}, Title: ${listing.title}")
                                // Add padding to ensure item is visible
                                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                    PropertyListingCard(
                                        listing = listing,
                                        isSelected = uiState.selectedListing?.id == listing.id,
                                        onSelect = { 
                                            println("DEBUG: Selected listing: ${listing.id}")
                                            viewModel.selectListing(listing) 
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                viewModel.saveGoal()
                                onContinue()
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
                    LaunchedEffect(uiState.isLoading, uiState.listings.size, uiState.errorMessage) {
                        println("DEBUG: GoalSelectionScreen - Reached else branch (unexpected state): isLoading=${uiState.isLoading}, listings=${uiState.listings.size}, error=${uiState.errorMessage}")
                    }
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
    // Debug: Log card composition
    println("DEBUG: PropertyListingCard composing for ID: ${listing.id}, Title: ${listing.title}")
    
    val httpClient: HttpClient = koinInject()
    var imageBitmap by remember(listing.id) { mutableStateOf<ImageBitmap?>(null) }
    var imageLoadingError by remember(listing.id) { mutableStateOf<String?>(null) }
    val imageUrl = listing.getMainImageUrl()
    
    // Debug: Log image URL
    println("DEBUG: PropertyListingCard - Image URL: $imageUrl")
    
    // Load image if URL is available
    LaunchedEffect(imageUrl) {
        if (imageUrl != null) {
            try {
                val imageBytes = withContext(Dispatchers.Default) {
                    httpClient.get(imageUrl) {
                        headers {
                            append(HttpHeaders.Accept, "image/*")
                        }
                    }.body<ByteArray>()
                }
                imageBitmap = SkiaImage.makeFromEncoded(imageBytes).toComposeImageBitmap()
                imageLoadingError = null
            } catch (e: Exception) {
                // Image loading failed, keep null to show placeholder
                imageBitmap = null
                imageLoadingError = e.message
            }
        }
    }
    
    // Debug: Ensure card has minimum height
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Property Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                if (!listing.title.isNullOrBlank()) {
                    Text(
                        text = listing.title!!,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Location
                Text(
                    text = listing.getLocationDisplay(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Price - prominently displayed
                if (listing.buyingPrice != null) {
                    Text(
                        text = listing.getFormattedPrice(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Size, rooms, and price per sqm
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (listing.squareMeter != null) {
                        Text(
                            text = listing.getFormattedSize(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    listing.rooms?.let {
                        Text(
                            text = listing.getFormattedRooms(),
                            style = MaterialTheme.typography.bodyMedium,
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
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

