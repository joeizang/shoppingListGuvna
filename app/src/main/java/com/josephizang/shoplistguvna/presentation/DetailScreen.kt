package com.josephizang.shoplistguvna.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.josephizang.shoplistguvna.data.local.ShoppingItem
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    viewModel: ListDetailViewModel,
    onBack: () -> Unit
) {
    val currentList by viewModel.currentList.collectAsState()
    val items by viewModel.items.collectAsState()
    var showAddItemSheet by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ShoppingItem?>(null) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateListName by remember { mutableStateOf("") }

    // Currency formatter


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentList?.name ?: "Loading...",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },

                actions = {
                    val isArchived = currentList?.isArchived == true
                    IconButton(onClick = {
                        duplicateListName = "${currentList?.name ?: ""} (Copy)"
                        showDuplicateDialog = true
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate List")
                    }
                    IconButton(onClick = {
                        if (isArchived) viewModel.unarchiveList() else viewModel.archiveList()
                        onBack()
                    }) {
                        Icon(
                            if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            contentDescription = if (isArchived) "Unarchive List" else "Archive List"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddItemSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Spent",
                            style = MaterialTheme.typography.labelMedium
                        )
                        ResponsiveMoneyText(
                             amount = currentList?.totalBought ?: 0.0,
                             color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "Total",
                            style = MaterialTheme.typography.labelMedium
                        )
                        ResponsiveMoneyText(
                             amount = currentList?.totalEstimated ?: 0.0,
                             color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .testTag("list_detail_screen"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(items) { item ->
                ShoppingItemRow(
                    item = item,
                    onToggle = { viewModel.toggleItemChecked(item) },
                    onDelete = { viewModel.deleteItem(item) },
                    onEdit = { itemToEdit = item }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )
            }
        }
    }

    if (showAddItemSheet) {
        ItemFormSheet(
            title = "Add Item",
            actionLabel = "Add",
            onDismiss = { showAddItemSheet = false },
            onSubmit = { name, quantity, price ->
                viewModel.addItem(name, quantity, price)
                showAddItemSheet = false
            }
        )
    }

    itemToEdit?.let { editing ->
        ItemFormSheet(
            title = "Edit Item",
            actionLabel = "Save",
            initialName = editing.name,
            initialQuantity = editing.quantity.toString(),
            initialPrice = if (editing.pricePerUnit == 0.0) "" else editing.pricePerUnit.toString(),
            onDismiss = { itemToEdit = null },
            onSubmit = { name, quantity, price ->
                viewModel.updateItem(editing, name, quantity, price)
                itemToEdit = null
            }
        )
    }

    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text("Duplicate List") },
            text = {
                Column {
                    Text("Enter a name for the duplicated list:")
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = duplicateListName,
                        onValueChange = { duplicateListName = it },
                        label = { Text("List Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("duplicate_name_input"),
                        isError = duplicateListName.isBlank()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.duplicateList(duplicateListName)
                        showDuplicateDialog = false
                    },
                    enabled = duplicateListName.isNotBlank()
                ) {
                    Text("Duplicate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "NG"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                )
            )
            Text(
                text = "${item.quantity} x ${format.format(item.pricePerUnit)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = format.format(item.totalPrice),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormSheet(
    title: String,
    actionLabel: String,
    initialName: String = "",
    initialQuantity: String = "1",
    initialPrice: String = "",
    onDismiss: () -> Unit,
    onSubmit: (String, Int, Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember(initialName) { mutableStateOf(initialName) }
    var quantity by remember(initialQuantity) { mutableStateOf(initialQuantity) }
    var price by remember(initialPrice) { mutableStateOf(initialPrice) }

    val quantityValid = quantity.toIntOrNull()?.let { it >= 1 } == true
    val priceValid = price.isEmpty() || price.toDoubleOrNull() != null
    val submitEnabled = name.isNotBlank() && quantityValid && priceValid

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("item_form_sheet")
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth().testTag("item_name_input"),
                singleLine = true,
                isError = name.isBlank()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = quantity,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f).testTag("item_quantity_input"),
                    singleLine = true,
                    isError = !quantityValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (₦)") },
                    modifier = Modifier.weight(1f).testTag("item_price_input"),
                    singleLine = true,
                    isError = price.isNotEmpty() && !priceValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 1
                    val prc = price.toDoubleOrNull() ?: 0.0
                    onSubmit(name, qty, prc)
                },
                enabled = submitEnabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun ResponsiveMoneyText(amount: Double, color: Color) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "NG"))
    val text = format.format(amount)
    
    val style = when {
        text.length > 15 -> MaterialTheme.typography.titleSmall
        text.length > 11 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleLarge
    }

    Text(
        text = text,
        style = style.copy(
            fontWeight = FontWeight.Bold,
            color = color
        ),
        maxLines = 1
    )
}
