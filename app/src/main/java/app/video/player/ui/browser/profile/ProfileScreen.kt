/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.ui.browser.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import app.video.player.presentation.components.AppAlertDialog as AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import app.video.player.presentation.components.AppTextField as OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import app.video.player.presentation.components.AppDialogTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.video.player.R
import app.video.player.preferences.AppearancePreferences
import app.video.player.preferences.preference.collectAsState
import app.video.player.presentation.Screen
import app.video.player.presentation.components.pullrefresh.PullRefreshBox
import app.video.player.ui.browser.LocalNavigationBarHeight
import app.video.player.ui.browser.components.BrowserTopBar
import app.video.player.ui.icons.AppIcon
import app.video.player.ui.icons.Icon
import app.video.player.ui.icons.Icons
import app.video.player.ui.player.controls.components.tvFocusHighlight
import app.video.player.ui.preferences.PreferencesScreen
import app.video.player.ui.preferences.ProfileWatchStatistics
import app.video.player.ui.theme.AppMotion
import app.video.player.ui.theme.AppShapeScale
import app.video.player.ui.theme.onWallpaper
import app.video.player.ui.theme.wallpaperAwareBackgroundColor
import app.video.player.ui.utils.LocalBackStack
import app.video.player.ui.utils.navigateTo
import app.video.player.ui.utils.rememberAppHaptics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import java.io.File

private const val AVATAR_FILE_PREFIX = "profile_avatar_"
private const val AVATAR_SIZE_PX = 512

/** Personal hub with profile info and watching statistics. */
@Serializable
object ProfileScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val backStack = LocalBackStack.current
    val scope = rememberCoroutineScope()
    val haptics = rememberAppHaptics()
    val appearancePreferences = koinInject<AppearancePreferences>()
    val profileName by appearancePreferences.profileName.collectAsState()
    val profileImagePath by appearancePreferences.profileImagePath.collectAsState()
    val configuration = LocalConfiguration.current
    val navigationBarHeight = LocalNavigationBarHeight.current

    var showProfileDialog by rememberSaveable { mutableStateOf(false) }
    val isRefreshing = remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showHeaderAvatar by remember {
      derivedStateOf {
        listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 160
      }
    }
    val heroCollapseProgress by remember {
      derivedStateOf {
        if (listState.firstVisibleItemIndex > 0) 1f
        else (listState.firstVisibleItemScrollOffset / 180f).coerceIn(0f, 1f)
      }
    }
    val reduceMotion = AppMotion.shouldReduceMotion()
    val headerAlphaSpec = if (reduceMotion) AppMotion.ReducedAlpha else AppMotion.Effect.Alpha
    val headerOffsetSpec = if (reduceMotion) AppMotion.ReducedOffset else AppMotion.Spatial.ExpressiveOffset
    val headerScaleSpec = if (reduceMotion) AppMotion.ReducedAlpha else AppMotion.Spatial.ExpressiveDefault

    Scaffold(
      containerColor = wallpaperAwareBackgroundColor(),
      topBar = {
        BrowserTopBar(
          title = stringResource(R.string.ui_profile),
          isInSelectionMode = false,
          selectedCount = 0,
          totalCount = 0,
          onCancelSelection = { },
          onSettingsClick = { backStack.navigateTo(PreferencesScreen) },
          customTitle = {
            AnimatedContent(
              targetState = showHeaderAvatar,
              transitionSpec = {
                if (reduceMotion) {
                  fadeIn(headerAlphaSpec).togetherWith(fadeOut(headerAlphaSpec))
                } else if (targetState) {
                  (fadeIn(headerAlphaSpec) + slideInVertically(headerOffsetSpec) { it / 2 } + scaleIn(headerScaleSpec, initialScale = 0.86f))
                    .togetherWith(fadeOut(headerAlphaSpec) + slideOutVertically(headerOffsetSpec) { -it / 2 })
                } else {
                  (fadeIn(headerAlphaSpec) + slideInVertically(headerOffsetSpec) { -it / 2 })
                    .togetherWith(fadeOut(headerAlphaSpec) + slideOutVertically(headerOffsetSpec) { it / 2 } + scaleOut(headerScaleSpec, targetScale = 0.86f))
                }
              },
              label = "profileHeaderTransition",
            ) { isCollapsed ->
              if (isCollapsed) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier =
                    Modifier
                      .clip(CircleShape)
                      .clickable {
                        scope.launch { listState.animateScrollToItem(0) }
                      }
                      .padding(vertical = 4.dp, horizontal = 4.dp),
                ) {
                  ProfileAvatar(
                    size = 36.dp,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentDescription = profileName,
                  )
                  Text(
                    text = profileName.ifBlank { stringResource(R.string.ui_profile) },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.onWallpaper(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              } else {
                Text(
                  text = stringResource(R.string.ui_profile),
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.primary.onWallpaper(),
                  modifier = Modifier.padding(start = 8.dp),
                )
              }
            }
          },
        )
      },
    ) { paddingValues ->
      PullRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
          isRefreshing.value = false
        },
        listState = listState,
        modifier = Modifier.fillMaxSize().padding(paddingValues),
      ) {
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(top = 0.dp, bottom = navigationBarHeight + 16.dp),
          verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
          item(key = "profile_overview") {
            ProfileOverviewCard(
              name = profileName,
              collapseProgress = heroCollapseProgress,
              onEditProfile = {
                haptics.tick()
                showProfileDialog = true
              },
            )
          }

          item(key = "watch_statistics") {
            ProfileWatchStatistics()
          }
        }
      }
    }

    if (showProfileDialog) {
      ProfileEditDialog(
        initialName = profileName,
        hasPhoto = profileImagePath.isNotBlank(),
        onDismiss = { showProfileDialog = false },
        onSave = { name, newPhoto, removePhoto ->
          showProfileDialog = false
          appearancePreferences.profileName.set(name.trim())
          if (newPhoto != null || removePhoto) {
            scope.launch {
              val path = withContext(Dispatchers.IO) { storeProfileAvatar(context, newPhoto) }
              appearancePreferences.profileImagePath.set(path)
            }
          }
        },
      )
    }
  }
}

/** The user's avatar, or the account glyph when none is set. Shared with the navigation pill and header. */
@Composable
internal fun ProfileAvatar(
  size: Dp,
  tint: Color,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  containerColor: Color? = null,
) {
  val path by koinInject<AppearancePreferences>().profileImagePath.collectAsState()
  val avatar = rememberProfileAvatar(path)
  if (avatar != null) {
    Image(
      bitmap = avatar,
      contentDescription = contentDescription,
      contentScale = ContentScale.Crop,
      modifier = modifier.size(size).clip(CircleShape),
    )
  } else if (containerColor != null) {
    Surface(
      shape = CircleShape,
      color = containerColor,
      modifier = modifier.size(size),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          Icons.RoundedFilled.Person,
          contentDescription = contentDescription,
          tint = tint,
          modifier = Modifier.size(size * 0.54f),
        )
      }
    }
  } else {
    Icon(
      Icons.RoundedFilled.AccountCircle,
      contentDescription = contentDescription,
      tint = tint,
      modifier = modifier.size(size),
    )
  }
}

@Composable
private fun rememberProfileAvatar(path: String): ImageBitmap? {
  val avatar by produceState<ImageBitmap?>(initialValue = null, path) {
    value =
      if (path.isBlank()) {
        null
      } else {
        withContext(Dispatchers.IO) {
          runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
        }
      }
  }
  return avatar
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileOverviewCard(
  name: String,
  collapseProgress: Float,
  onEditProfile: () -> Unit,
) {
  val configuration = LocalConfiguration.current
  val isTablet = configuration.smallestScreenWidthDp >= 600
  val editLabel = stringResource(R.string.profile_edit)
  val avatarScale = 1f - 0.25f * collapseProgress
  val avatarAlpha = (1f - collapseProgress * 1.5f).coerceIn(0f, 1f)

  val horizontalPadding = if (isTablet) 24.dp else 16.dp

  Column(
    modifier = Modifier.fillMaxWidth().padding(start = horizontalPadding, end = horizontalPadding, top = 2.dp, bottom = 0.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 0.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Box(
        contentAlignment = Alignment.BottomEnd,
        modifier =
          Modifier.graphicsLayer {
            scaleX = avatarScale
            scaleY = avatarScale
            alpha = avatarAlpha
          },
      ) {
        ProfileAvatar(
          size = if (isTablet) 116.dp else 96.dp,
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentDescription = null,
        )
        // A small visual badge at the lower-right edge, with an independent 48dp
        // touch target so the edit action remains easy to reach on small devices.
        Box(
          modifier = Modifier
            .size(48.dp)
            .offset(x = 6.dp, y = 6.dp)
            .tvFocusHighlight(CircleShape)
            .clickable(onClick = onEditProfile),
          contentAlignment = Alignment.Center,
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(28.dp),
            shadowElevation = 2.dp,
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.RoundedFilled.Edit,
                contentDescription = editLabel,
                modifier = Modifier.size(13.dp),
              )
            }
          }
        }
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        Text(
          text = name.ifBlank { stringResource(R.string.ui_profile) },
          modifier = Modifier.semantics { heading() },
          style = MaterialTheme.typography.titleLargeEmphasized,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditDialog(
  initialName: String,
  hasPhoto: Boolean,
  onDismiss: () -> Unit,
  onSave: (name: String, newPhoto: Bitmap?, removePhoto: Boolean) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val currentPath by koinInject<AppearancePreferences>().profileImagePath.collectAsState()
  val currentAvatar = rememberProfileAvatar(currentPath)
  var name by rememberSaveable { mutableStateOf(initialName) }
  var pendingPhoto by remember { mutableStateOf<Bitmap?>(null) }
  var rawCropBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var removePhoto by remember { mutableStateOf(false) }
  val pendingImage = remember(pendingPhoto) { pendingPhoto?.asImageBitmap() }
  val preview = pendingImage ?: currentAvatar.takeUnless { removePhoto }
  val showsPhoto = pendingPhoto != null || (hasPhoto && !removePhoto)

  val picker =
    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
      if (uri != null) {
        scope.launch {
          val decoded = withContext(Dispatchers.IO) { decodeRawProfileAvatar(context, uri) }
          if (decoded != null) {
            rawCropBitmap = decoded
          }
        }
      }
    }

  if (rawCropBitmap != null) {
    ProfileAvatarCropDialog(
      sourceBitmap = rawCropBitmap!!,
      onDismiss = { rawCropBitmap = null },
      onCropped = { cropped ->
        rawCropBitmap = null
        pendingPhoto = cropped
        removePhoto = false
      },
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.profile_edit)) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Surface(
          onClick = { picker.launch("image/*") },
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.size(96.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            if (preview != null) {
              Image(preview, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
              Icon(
                Icons.RoundedFilled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(52.dp),
              )
            }
          }
        }
        // Three buttons don't fit one row in the dialog width once a photo exists; a plain Row
        // squeezed the last one into a thin vertical pill. FlowRow wraps it onto its own line.
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          OutlinedButton(onClick = { picker.launch("image/*") }) {
            Text(stringResource(R.string.profile_choose_photo), maxLines = 1)
          }
          if (showsPhoto) {
            OutlinedButton(
              onClick = {
                val toCrop = pendingPhoto ?: currentPath.takeIf { it.isNotBlank() }?.let {
                  BitmapFactory.decodeFile(it)
                }
                if (toCrop != null) {
                  rawCropBitmap = toCrop
                }
              },
            ) {
              Text(stringResource(R.string.clip_crop), maxLines = 1)
            }
            OutlinedButton(
              onClick = {
                pendingPhoto = null
                removePhoto = true
              },
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
              ),
              border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
              ),
            ) {
              Text(stringResource(R.string.profile_remove_photo), maxLines = 1)
            }
          }
        }
        OutlinedTextField(
          value = name,
          onValueChange = { name = it.take(40) },
          label = { Text(stringResource(R.string.profile_display_name)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      TextButton(onClick = { onSave(name, pendingPhoto, removePhoto) }) {
        Text(stringResource(R.string.ui_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.generic_cancel)) }
    },
  )
}

@Composable
private fun ProfileAvatarCropDialog(
  sourceBitmap: Bitmap,
  onDismiss: () -> Unit,
  onCropped: (Bitmap) -> Unit,
) {
  val configuration = LocalConfiguration.current
  val density = LocalDensity.current
  val isTablet = configuration.smallestScreenWidthDp >= 600

  val cropBoxDp = minOf(configuration.screenWidthDp.dp - 64.dp, if (isTablet) 360.dp else 280.dp)
  val cropBoxPx = with(density) { cropBoxDp.toPx() }

  var rotation by remember { mutableStateOf(0) }
  var userScale by remember { mutableStateOf(1f) }
  var userOffset by remember { mutableStateOf(Offset.Zero) }

  val rotatedBitmap = remember(sourceBitmap, rotation) {
    if (rotation % 360 != 0) {
      val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
      Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true)
    } else {
      sourceBitmap
    }
  }

  val rotatedImageBitmap = remember(rotatedBitmap) { rotatedBitmap.asImageBitmap() }

  val baseScale = remember(rotatedImageBitmap, cropBoxPx) {
    maxOf(cropBoxPx / rotatedImageBitmap.width, cropBoxPx / rotatedImageBitmap.height)
  }

  // Calculate clamp boundaries so the image always completely covers the crop box
  val maxPanX = remember(userScale, baseScale, rotatedImageBitmap.width, cropBoxPx) {
    ((rotatedImageBitmap.width * baseScale * userScale - cropBoxPx) / 2f).coerceAtLeast(0f)
  }
  val maxPanY = remember(userScale, baseScale, rotatedImageBitmap.height, cropBoxPx) {
    ((rotatedImageBitmap.height * baseScale * userScale - cropBoxPx) / 2f).coerceAtLeast(0f)
  }

  // Keep offset clamped when zoom changes
  LaunchedEffect(maxPanX, maxPanY) {
    userOffset = Offset(
      userOffset.x.coerceIn(-maxPanX, maxPanX),
      userOffset.y.coerceIn(-maxPanY, maxPanY),
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier
          .fillMaxWidth(if (isTablet) 0.65f else 0.94f)
          .clip(AppShapeScale.large),
      shape = AppShapeScale.large,
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.clip_crop),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.RoundedFilled.Close, contentDescription = stringResource(R.string.generic_cancel))
          }
        }

        // Viewport
        Box(
          modifier =
            Modifier
              .size(cropBoxDp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Black)
              .pointerInput(rotatedImageBitmap, cropBoxPx) {
                detectTransformGestures { _, pan, zoom, _ ->
                  userScale = (userScale * zoom).coerceIn(1f, 5f)
                  val currentMaxPanX = ((rotatedImageBitmap.width * baseScale * userScale - cropBoxPx) / 2f).coerceAtLeast(0f)
                  val currentMaxPanY = ((rotatedImageBitmap.height * baseScale * userScale - cropBoxPx) / 2f).coerceAtLeast(0f)
                  userOffset = Offset(
                    (userOffset.x + pan.x).coerceIn(-currentMaxPanX, currentMaxPanX),
                    (userOffset.y + pan.y).coerceIn(-currentMaxPanY, currentMaxPanY),
                  )
                }
              },
          contentAlignment = Alignment.Center,
        ) {
          Image(
            bitmap = rotatedImageBitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
              Modifier
                .size(
                  width = with(density) { (rotatedImageBitmap.width * baseScale).toDp() },
                  height = with(density) { (rotatedImageBitmap.height * baseScale).toDp() },
                )
                .graphicsLayer {
                  scaleX = userScale
                  scaleY = userScale
                  translationX = userOffset.x
                  translationY = userOffset.y
                },
          )

          // Circular crop cutout mask overlay
          Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val radius = minOf(width, height) / 2f

            val path = Path().apply {
              fillType = PathFillType.EvenOdd
              addRect(Rect(0f, 0f, width, height))
              addOval(Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
            }
            drawPath(path, color = Color.Black.copy(alpha = 0.60f))

            drawCircle(
              color = Color.White.copy(alpha = 0.85f),
              radius = radius - 1f,
              center = center,
              style = Stroke(width = 2.dp.toPx()),
            )
          }
        }

        // Toolbar: Rotate Left, Rotate Right & Reset
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          FilledTonalButton(
            onClick = {
              rotation = (rotation - 90 + 360) % 360
              userScale = 1f
              userOffset = Offset.Zero
            },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Icon(Icons.RoundedFilled.RotateLeft, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Left", style = MaterialTheme.typography.labelMedium)
          }

          FilledTonalButton(
            onClick = {
              rotation = (rotation + 90) % 360
              userScale = 1f
              userOffset = Offset.Zero
            },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Icon(Icons.RoundedFilled.RotateRight, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Right", style = MaterialTheme.typography.labelMedium)
          }

          FilledTonalButton(
            onClick = {
              rotation = 0
              userScale = 1f
              userOffset = Offset.Zero
            },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Icon(Icons.RoundedFilled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.generic_reset), style = MaterialTheme.typography.labelMedium)
          }
        }

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.generic_cancel))
          }
          Button(
            onClick = {
              val cropped = performCrop(
                bitmap = sourceBitmap,
                rotation = rotation,
                userScale = userScale,
                userOffset = userOffset,
                viewportSizePx = cropBoxPx,
              )
              onCropped(cropped)
            },
          ) {
            Text(stringResource(R.string.clip_crop))
          }
        }
      }
    }
  }
}

private fun performCrop(
  bitmap: Bitmap,
  rotation: Int,
  userScale: Float,
  userOffset: Offset,
  viewportSizePx: Float,
  targetSizePx: Int = AVATAR_SIZE_PX,
): Bitmap {
  val rotated = if (rotation % 360 != 0) {
    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  } else {
    bitmap
  }

  val bw = rotated.width.toFloat()
  val bh = rotated.height.toFloat()

  val baseScale = maxOf(viewportSizePx / bw, viewportSizePx / bh)
  val totalScale = baseScale * userScale

  val cropSizeInImage = (viewportSizePx / totalScale).coerceAtMost(minOf(bw, bh))
  val cropCenterX = bw / 2f - userOffset.x / totalScale
  val cropCenterY = bh / 2f - userOffset.y / totalScale

  val srcX = (cropCenterX - cropSizeInImage / 2f).toInt().coerceIn(0, (bw - cropSizeInImage).toInt().coerceAtLeast(0))
  val srcY = (cropCenterY - cropSizeInImage / 2f).toInt().coerceIn(0, (bh - cropSizeInImage).toInt().coerceAtLeast(0))
  val srcW = cropSizeInImage.toInt().coerceAtMost(rotated.width - srcX)
  val srcH = cropSizeInImage.toInt().coerceAtMost(rotated.height - srcY)
  val cropSide = minOf(srcW, srcH).coerceAtLeast(1)

  val scaleMatrix = Matrix().apply {
    val scale = targetSizePx.toFloat() / cropSide
    postScale(scale, scale)
  }

  return Bitmap.createBitmap(rotated, srcX, srcY, cropSide, cropSide, scaleMatrix, true)
}

/** Decodes [uri] into an upright raw bitmap for cropping. */
private fun decodeRawProfileAvatar(
  context: Context,
  uri: Uri,
): Bitmap? =
  runCatching {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
    while (maxDim / (sampleSize * 2) >= 2048) sampleSize *= 2
    val decoded =
      resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
      } ?: return null

    val rotation =
      resolver.openInputStream(uri)?.use {
        when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
          ExifInterface.ORIENTATION_ROTATE_90 -> 90f
          ExifInterface.ORIENTATION_ROTATE_180 -> 180f
          ExifInterface.ORIENTATION_ROTATE_270 -> 270f
          else -> 0f
        }
      } ?: 0f

    if (rotation != 0f) {
      val matrix = Matrix().apply { postRotate(rotation) }
      Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    } else {
      decoded
    }
  }.getOrNull()

/** Replaces the stored avatar with [bitmap] (or clears it when null) and returns the new path. */
private fun storeProfileAvatar(
  context: Context,
  bitmap: Bitmap?,
): String {
  context.filesDir.listFiles { file -> file.name.startsWith(AVATAR_FILE_PREFIX) }?.forEach(File::delete)
  if (bitmap == null) return ""
  // A fresh name per save changes the preference value, so every avatar observer reloads.
  val file = File(context.filesDir, "$AVATAR_FILE_PREFIX${System.currentTimeMillis()}.png")
  file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
  return file.absolutePath
}
