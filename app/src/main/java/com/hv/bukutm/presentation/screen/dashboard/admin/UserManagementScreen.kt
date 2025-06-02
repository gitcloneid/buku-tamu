package com.hv.bukutm.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hv.bukutm.R
import com.hv.bukutm.domain.model.User
import com.hv.bukutm.ui.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: UserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showEditUserDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    var createName by remember { mutableStateOf("") }
    var createEmail by remember { mutableStateOf("") }
    var createPassword by remember { mutableStateOf("") }
    var createRole by remember { mutableStateOf("Guru") }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editRole by remember { mutableStateOf("Guru") }

    val roles = listOf("Guru", "PenerimaTamu")

    // Gunakan warna dari MaterialTheme.colorScheme untuk mendukung tema dinamis
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val textColor = MaterialTheme.colorScheme.onSurface
    val accentColor = MaterialTheme.colorScheme.secondary
    val cardBackgroundColor = MaterialTheme.colorScheme.surface // Untuk Card dan dialog
    val dialogBackgroundColor = MaterialTheme.colorScheme.surface

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    actionLabel = if (error.contains("koneksi", true)) "Coba Lagi" else null,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Search debounce
    LaunchedEffect(searchQuery) {
        delay(300)
        viewModel.searchUsers(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Manajemen Pengguna",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                containerColor = primaryColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pengguna")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .background(MaterialTheme.colorScheme.background) // Pastikan background sesuai tema
        ) {
            // Search and Filter Row
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari nama atau email", color = textColor) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = primaryColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBackgroundColor, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = primaryColor,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = textColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(uiState.roleFilter ?: "Semua Peran", color = textColor)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Filter", tint = primaryColor)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(cardBackgroundColor)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Peran", color = textColor) },
                            onClick = {
                                viewModel.setRoleFilter(null)
                                expanded = false
                            }
                        )
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role, color = textColor) },
                                onClick = {
                                    viewModel.setRoleFilter(role)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }

            AnimatedVisibility(
                visible = !uiState.isLoading && uiState.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ErrorMessage(
                    message = uiState.error ?: "Terjadi kesalahan",
                    onRetry = { viewModel.retry() },
                    errorColor = errorColor,
                    textColor = textColor,
                    primaryColor = primaryColor
                )
            }

            AnimatedVisibility(
                visible = !uiState.isLoading && uiState.error == null && uiState.filteredUsers.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyState(
                    message = if (searchQuery.isBlank()) "Belum ada pengguna" else "Tidak ada hasil pencarian",
                    textColor = textColor
                )
            }

            AnimatedVisibility(
                visible = !uiState.isLoading && uiState.error == null && uiState.filteredUsers.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredUsers) { user ->
                        UserCard(
                            user = user,
                            onEdit = {
                                selectedUser = user
                                editName = user.nama
                                editEmail = user.email
                                editRole = user.role
                                showEditUserDialog = true
                            },
                            onDelete = {
                                selectedUser = user
                                viewModel.showDeleteDialog(true, user)
                            },
                            primaryColor = primaryColor,
                            errorColor = errorColor,
                            textColor = textColor
                        )
                    }
                }
            }
        }

        // Add User Dialog
        if (showAddUserDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddUserDialog = false
                    createName = ""
                    createEmail = ""
                    createPassword = ""
                    createRole = "Guru"
                    viewModel.showCreateDialog(false)
                },
                title = {
                    Text(
                        "Tambah Pengguna",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = createName,
                            onValueChange = { createName = it },
                            label = { Text("Nama Lengkap", color = textColor) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBackgroundColor, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            isError = uiState.validationErrors.containsKey("nama"),
                            supportingText = {
                                uiState.validationErrors["nama"]?.let { Text(it, color = errorColor) }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = primaryColor,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = primaryColor,
                                focusedLabelColor = primaryColor,
                                unfocusedLabelColor = textColor
                            )
                        )

                        OutlinedTextField(
                            value = createEmail,
                            onValueChange = { createEmail = it },
                            label = { Text("Email", color = textColor) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBackgroundColor, RoundedCornerShape(12.dp)),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            isError = uiState.validationErrors.containsKey("email"),
                            supportingText = {
                                uiState.validationErrors["email"]?.let { Text(it, color = errorColor) }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = primaryColor,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = primaryColor,
                                focusedLabelColor = primaryColor,
                                unfocusedLabelColor = textColor
                            )
                        )

                        var showPassword by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = createPassword,
                            onValueChange = { createPassword = it },
                            label = { Text("Kata Sandi", color = textColor) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBackgroundColor, RoundedCornerShape(12.dp)),
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        painter = painterResource(id = if (showPassword) R.drawable.eye_open_svgrepo_com else R.drawable.eye_closed_svgrepo_com),
                                        contentDescription = if (showPassword) "Sembunyikan kata sandi" else "Tampilkan kata sandi",
                                        modifier = Modifier.size(20.dp),
                                        tint = primaryColor
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            isError = uiState.validationErrors.containsKey("password"),
                            supportingText = {
                                uiState.validationErrors["password"]?.let { Text(it, color = errorColor) }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = primaryColor,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = primaryColor,
                                focusedLabelColor = primaryColor,
                                unfocusedLabelColor = textColor
                            )
                        )

                        Text("Peran", style = MaterialTheme.typography.titleMedium.copy(color = textColor))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            roles.forEach { role ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { createRole = role }
                                        .padding(vertical = 8.dp)
                                ) {
                                    RadioButton(
                                        selected = createRole == role,
                                        onClick = { createRole = role },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = primaryColor,
                                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(role, color = textColor)
                                }
                            }
                        }
                        uiState.validationErrors["role"]?.let {
                            Text(it, color = errorColor)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.createUser(createName, createEmail, createPassword, createRole)
                            showAddUserDialog = false
                        },
                        enabled = createName.isNotBlank() && createEmail.isNotBlank() &&
                                createPassword.isNotBlank() && createPassword.length >= 6,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tambah")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showAddUserDialog = false
                            createName = ""
                            createEmail = ""
                            createPassword = ""
                            createRole = "Guru"
                            viewModel.showCreateDialog(false)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = textColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batal")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = dialogBackgroundColor
            )
        }

        // Edit User Dialog
        if (showEditUserDialog && selectedUser != null) {
            AlertDialog(
                onDismissRequest = {
                    showEditUserDialog = false
                    viewModel.showEditDialog(null)
                },
                title = {
                    Text(
                        "Edit Pengguna",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nama Lengkap", color = textColor) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBackgroundColor, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            isError = uiState.validationErrors.containsKey("nama"),
                            supportingText = {
                                uiState.validationErrors["nama"]?.let { Text(it, color = errorColor) }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = primaryColor,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = primaryColor,
                                focusedLabelColor = primaryColor,
                                unfocusedLabelColor = textColor
                            )
                        )

                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email", color = textColor) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBackgroundColor, RoundedCornerShape(12.dp)),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            isError = uiState.validationErrors.containsKey("email"),
                            supportingText = {
                                uiState.validationErrors["email"]?.let { Text(it, color = errorColor) }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = primaryColor,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = primaryColor,
                                focusedLabelColor = primaryColor,
                                unfocusedLabelColor = textColor
                            )
                        )

                        Text("Peran", style = MaterialTheme.typography.titleMedium.copy(color = textColor))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            roles.forEach { role ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { editRole = role }
                                        .padding(vertical = 8.dp)
                                ) {
                                    RadioButton(
                                        selected = editRole == role,
                                        onClick = { editRole = role },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = primaryColor,
                                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(role, color = textColor)
                                }
                            }
                        }
                        uiState.validationErrors["role"]?.let {
                            Text(it, color = errorColor)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateUser(
                                selectedUser!!.idPengguna,
                                editName,
                                editEmail,
                                editRole
                            )
                            showEditUserDialog = false
                        },
                        enabled = editName.isNotBlank() && editEmail.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showEditUserDialog = false
                            viewModel.showEditDialog(null)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = textColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batal")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = dialogBackgroundColor
            )
        }

        // Delete Confirmation Dialog
        if (uiState.showDeleteDialog && uiState.selectedUser != null) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteDialog(false) },
                title = {
                    Text(
                        "Konfirmasi Hapus",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                },
                text = { Text("Apakah Anda yakin ingin menghapus ${uiState.selectedUser?.nama}?", color = textColor) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteUser(uiState.selectedUser!!.idPengguna)
                            viewModel.showDeleteDialog(false)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = errorColor,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.showDeleteDialog(false) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = textColor
                        )
                    ) {
                        Text("Batal")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = dialogBackgroundColor
            )
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    primaryColor: Color,
    errorColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            contentColor = textColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = user.nama,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor.copy(alpha = 0.7f)
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = when (user.role) {
                                        "Guru" -> primaryColor
                                        "PenerimaTamu" -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = when (user.role) {
                                    "Guru" -> "Guru"
                                    "PenerimaTamu" -> "Penerima Tamu"
                                    else -> user.role
                                },
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
                Row {
                    IconButton(
                        onClick = onEdit,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = primaryColor
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = errorColor
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus")
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    errorColor: Color,
    textColor: Color,
    primaryColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .background(MaterialTheme.colorScheme.background), // Pastikan background sesuai tema
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Kesalahan",
                modifier = Modifier.size(48.dp),
                tint = errorColor
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor,
                    fontSize = 16.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (message.contains("koneksi", true)) {
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Coba Lagi")
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .background(MaterialTheme.colorScheme.background), // Pastikan background sesuai tema
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Kosong",
                modifier = Modifier.size(48.dp),
                tint = textColor.copy(alpha = 0.4f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 16.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}