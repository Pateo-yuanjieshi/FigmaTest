package com.porsche.model.hmisdk.v2.button

import android.os.SystemClock
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.min
import com.porsche.model.hmisdk.Constants
import com.porsche.model.hmisdk.Constants.ANIM_FACTOR
import com.porsche.model.hmisdk.Constants.LONG_PRESS_TIMEOUT
import com.porsche.model.hmisdk.Constants.PLAY_SOUND_DEFAULT
import com.porsche.model.hmisdk.Constants.THEME_CHANGE_ANIM_DEFAULT
import com.porsche.model.hmisdk.Constants.THEME_CHANGE_ANIM_DELAY_TIME
import com.porsche.model.hmisdk.PorscheTouchAreaEnhance
import com.porsche.model.hmisdk.R
import com.porsche.model.hmisdk.porschePressedGesturesWithLongClick
import com.porsche.model.hmisdk.porschePressedLayer
import com.porsche.model.hmisdk.util.PorscheHmiThemeUtils
import com.porsche.model.hmisdk.util.SoundUtil
import com.porsche.model.hmisdk.v2.anim.PorscheCubicBezier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PorscheCustomButton(
    modifier: Modifier = Modifier,
    state: ButtonState = rememberButtonState(),
    style: ButtonStyle,
    theme: ButtonTheme = ButtonTheme.PrimaryTheme,
    animSpec: ButtonAnimSpec = ButtonAnimSpec.Default,
    themeChangeAnimEnable: Boolean = THEME_CHANGE_ANIM_DEFAULT,
    enable: Boolean = true,
    // 按钮不可用时是否回调
    callbackIfNotEnable: Boolean = false,
    playSound: Boolean = PLAY_SOUND_DEFAULT,
    // 点击热区
    touchTargetWidth: Dp = style.attribute.width,
    touchTargetHeight: Dp = style.attribute.height,
    // 按压态效果，默认开启
    pressAnimEnable: Boolean = true,
    // 暗色主题（非白）按下的蒙层颜色
    pressDarkThemeColor: Color? = null,
    // 白色主题按下的蒙层颜色
    pressLightThemeColor: Color? = null,
    // 宽度固定，默认固定，false则用width表示最小宽度
    widthFixed: Boolean = true,
    // 防抖动时间，单位毫秒，默认0不防抖 (只对onClick有效果，onPress/onRelease/onLongClick不受影响)
    debounceTime: Long = 0L,
    // 按压态是否忽略滚动手势，默认忽略
    pressStateIgnoreScroll: Boolean = true,
    // 按钮是否支持选中态，UI不会有任何变化，但dump出来会有个checked属性，用于可见即可说切换状态
    toggleable: Boolean = false,
    toggleValue: Boolean = false,
    // 按下的回调
    onPress: () -> Unit = {},
    // 松开的回调
    onRelease: () -> Unit = {},
    // 长按的回调
    onLongClick: () -> Unit = {},
    // 点击的回调
    onClick: () -> Unit = {},
    // 自定义内容
    customContent: @Composable (() -> Unit),
) {
    CustomContentButton(
        modifier = modifier,
        style = style,
        theme = theme,
        animSpec = animSpec,
        themeChangeAnimEnable = themeChangeAnimEnable,
        state = state,
        enable = enable,
        callbackIfNotEnable = callbackIfNotEnable,
        playSound = playSound,
        customContent = customContent,
        onPress = onPress,
        onRelease = onRelease,
        onLongClick = onLongClick,
        onClick = onClick,
        touchTargetWidth = touchTargetWidth,
        touchTargetHeight = touchTargetHeight,
        pressAnimEnable = pressAnimEnable,
        pressDarkThemeColor = pressDarkThemeColor,
        pressLightThemeColor = pressLightThemeColor,
        widthFixed = widthFixed,
        debounceTime = debounceTime,
        pressStateIgnoreScroll = pressStateIgnoreScroll,
        toggleable = toggleable,
        toggleValue = toggleValue
    )
}

@Composable
fun PorscheButtonEnableFastSwitch(
    modifier: Modifier = Modifier,
    showIcon: Boolean,
    showText: Boolean,
    image: Int? = null,
    text: String = "",
    state: ButtonState = rememberButtonState(),
    contentDescription: String = "",
    style: ButtonStyle,
    theme: ButtonTheme = ButtonTheme.PrimaryTheme,
    animSpec: ButtonAnimSpec = ButtonAnimSpec.Default,
    themeChangeAnimEnable: Boolean = THEME_CHANGE_ANIM_DEFAULT,
    enable: Boolean = true,
    // 是否宽度固定
    // true：attribute里的width代表固定宽度，
    // false：代表最小宽度，根据内容自适应
    widthFixed: Boolean = false,
    // 按钮不可用时是否回调
    callbackIfNotEnable: Boolean = false,
    playSound: Boolean = PLAY_SOUND_DEFAULT,
    // 点击热区
    touchTargetWidth: Dp = style.attribute.width,
    touchTargetHeight: Dp = style.attribute.height,
    // 按压态效果，默认开启
    pressAnimEnable: Boolean = true,
    // 暗色主题（非白）按下的蒙层颜色
    pressDarkThemeColor: Color? = null,
    // 白色主题按下的蒙层颜色
    pressLightThemeColor: Color? = null,
    // 防抖动时间，单位毫秒，默认0不防抖 (只对onClick有效果，onPress/onRelease/onLongClick不受影响)
    debounceTime: Long = 0L,
    // 按压态是否忽略滚动手势，默认忽略
    pressStateIgnoreScroll: Boolean = true,
    // 按钮是否支持选中态，UI不会有任何变化，但dump出来会有个checked属性，用于可见即可说切换状态
    toggleable: Boolean = false,
    toggleValue: Boolean = false,
    // 按下的回调
    onPress: () -> Unit = {},
    // 松开的回调
    onRelease: () -> Unit = {},
    // 长按的回调
    onLongClick: () -> Unit = {},
    // 点击的回调
    onClick: () -> Unit = {}
) {
    if (enable) {
        PorscheButton(
            modifier = modifier,
            showIcon = showIcon,
            showText = showText,
            image = image,
            text = text,
            state = state,
            contentDescription = contentDescription,
            style = style,
            theme = theme,
            animSpec = animSpec,
            themeChangeAnimEnable = themeChangeAnimEnable,
            enable = true,
            widthFixed = widthFixed,
            callbackIfNotEnable = callbackIfNotEnable,
            playSound = playSound,
            touchTargetWidth = touchTargetWidth,
            touchTargetHeight = touchTargetHeight,
            pressAnimEnable = pressAnimEnable,
            pressDarkThemeColor = pressDarkThemeColor,
            pressLightThemeColor = pressLightThemeColor,
            debounceTime = debounceTime,
            pressStateIgnoreScroll = pressStateIgnoreScroll,
            toggleable = toggleable,
            toggleValue = toggleValue,
            onPress = onPress,
            onRelease = onRelease,
            onLongClick = onLongClick,
            onClick = onClick
        )
    } else {
        PorscheButton(
            modifier = modifier,
            showIcon = showIcon,
            showText = showText,
            image = image,
            text = text,
            state = state,
            contentDescription = contentDescription,
            style = style,
            theme = theme,
            animSpec = animSpec,
            themeChangeAnimEnable = themeChangeAnimEnable,
            enable = false,
            widthFixed = widthFixed,
            callbackIfNotEnable = callbackIfNotEnable,
            playSound = playSound,
            touchTargetWidth = touchTargetWidth,
            touchTargetHeight = touchTargetHeight,
            pressAnimEnable = pressAnimEnable,
            pressDarkThemeColor = pressDarkThemeColor,
            pressLightThemeColor = pressLightThemeColor,
            debounceTime = debounceTime,
            pressStateIgnoreScroll = pressStateIgnoreScroll,
            toggleable = toggleable,
            toggleValue = toggleValue,
            onPress = onPress,
            onRelease = onRelease,
            onLongClick = onLongClick,
            onClick = onClick
        )
    }
}

@Composable
fun PorscheButton(
    modifier: Modifier = Modifier,
    showIcon: Boolean,
    showText: Boolean,
    image: Int? = null,
    text: String = "",
    state: ButtonState = rememberButtonState(),
    contentDescription: String = "",
    style: ButtonStyle,
    theme: ButtonTheme = ButtonTheme.PrimaryTheme,
    animSpec: ButtonAnimSpec = ButtonAnimSpec.Default,
    themeChangeAnimEnable: Boolean = THEME_CHANGE_ANIM_DEFAULT,
    enable: Boolean = true,
    // 是否宽度固定
    // true：attribute里的width代表固定宽度，
    // false：代表最小宽度，根据内容自适应
    widthFixed: Boolean = false,
    // 按钮不可用时是否回调
    callbackIfNotEnable: Boolean = false,
    playSound: Boolean = PLAY_SOUND_DEFAULT,
    // 点击热区
    touchTargetWidth: Dp = style.attribute.width,
    touchTargetHeight: Dp = style.attribute.height,
    // 按压态效果，默认开启
    pressAnimEnable: Boolean = true,
    // 暗色主题（非白）按下的蒙层颜色
    pressDarkThemeColor: Color? = null,
    // 白色主题按下的蒙层颜色
    pressLightThemeColor: Color? = null,
    // 防抖动时间，单位毫秒，默认0不防抖 (只对onClick有效果，onPress/onRelease/onLongClick不受影响)
    debounceTime: Long = 0L,
    // 按压态是否忽略滚动手势，默认忽略
    pressStateIgnoreScroll: Boolean = true,
    // 按钮是否支持选中态，UI不会有任何变化，但dump出来会有个checked属性，用于可见即可说切换状态
    toggleable: Boolean = false,
    toggleValue: Boolean = false,
    // 按下的回调
    onPress: () -> Unit = {},
    // 松开的回调
    onRelease: () -> Unit = {},
    // 长按的回调
    onLongClick: () -> Unit = {},
    // 点击的回调
    onClick: () -> Unit = {}
){

    when {
        showIcon && showText && image != null -> {
            IconTextButton(
                text = text,
                image = image,
                contentDesc = contentDescription,
                style = style,
                state = state,
                modifier = modifier,
                theme = theme,
                animSpec = animSpec,
                enable = enable,
                widthFixed = widthFixed,
                callbackIfNotEnable = callbackIfNotEnable,
                onPress = onPress,
                onRelease = onRelease,
                onLongClick = onLongClick,
                onClick = onClick,
                playSound = playSound,
                themeChangeAnimEnable = themeChangeAnimEnable,
                touchTargetWidth = touchTargetWidth,
                touchTargetHeight = touchTargetHeight,
                pressAnimEnable = pressAnimEnable,
                pressDarkThemeColor = pressDarkThemeColor,
                pressLightThemeColor = pressLightThemeColor,
                debounceTime = debounceTime,
                pressStateIgnoreScroll = pressStateIgnoreScroll,
                toggleable = toggleable,
                toggleValue = toggleValue
            )
        }
        showIcon && image != null -> {
            IconButton(
                modifier = modifier,
                image = image,
                contentDesc = contentDescription,
                theme = theme,
                animSpec = animSpec,
                style = style,
                state = state,
                enable = enable,
                callbackIfNotEnable = callbackIfNotEnable,
                onPress = onPress,
                onRelease = onRelease,
                onLongClick = onLongClick,
                onClick = onClick,
                playSound = playSound,
                themeChangeAnimEnable = themeChangeAnimEnable,
                touchTargetWidth = touchTargetWidth,
                touchTargetHeight = touchTargetHeight,
                pressAnimEnable = pressAnimEnable,
                pressDarkThemeColor = pressDarkThemeColor,
                pressLightThemeColor = pressLightThemeColor,
                debounceTime = debounceTime,
                pressStateIgnoreScroll = pressStateIgnoreScroll,
                toggleable = toggleable,
                toggleValue = toggleValue
            )
        }
        showText -> {
            TextButton(
                modifier = modifier,
                text = text,
                contentDesc = contentDescription,
                theme = theme,
                animSpec = animSpec,
                style = style,
                state = state,
                widthFixed = widthFixed,
                enable = enable,
                callbackIfNotEnable = callbackIfNotEnable,
                onPress = onPress,
                onRelease = onRelease,
                onLongClick = onLongClick,
                onClick = onClick,
                playSound = playSound,
                themeChangeAnimEnable = themeChangeAnimEnable,
                touchTargetWidth = touchTargetWidth,
                touchTargetHeight = touchTargetHeight,
                pressAnimEnable = pressAnimEnable,
                pressDarkThemeColor = pressDarkThemeColor,
                pressLightThemeColor = pressLightThemeColor,
                debounceTime = debounceTime,
                pressStateIgnoreScroll = pressStateIgnoreScroll,
                toggleable = toggleable,
                toggleValue = toggleValue
            )
        }
    }
}

/**
 * 文本Button
 */
@Composable
private fun TextButton(
    modifier: Modifier = Modifier,
    text: String,
    style: ButtonStyle,
    theme: ButtonTheme,
    animSpec: ButtonAnimSpec = ButtonAnimSpec.Default,
    themeChangeAnimEnable: Boolean = THEME_CHANGE_ANIM_DEFAULT,
    state: ButtonState,
    widthFixed: Boolean = false,
    enable: Boolean = true,
    callbackIfNotEnable: Boolean = false,
    playSound: Boolean = PLAY_SOUND_DEFAULT,
    touchTargetWidth: Dp = style.attribute.width,
    touchTargetHeight: Dp = style.attribute.height,
    pressAnimEnable: Boolean = true,
    pressDarkThemeColor: Color? = null,
    pressLightThemeColor: Color? = null,
    debounceTime: Long = 0L,
    // 按压态是否忽略滚动手势，默认忽略
    pressStateIgnoreScroll: Boolean = true,
    toggleable: Boolean = false,
    toggleValue: Boolean = false,
    contentDesc: String = "",
    onPress: () -> Unit = {},
    onRelease: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val onClickState = rememberUpdatedState(onClick)
    val onLongClickState = rememberUpdatedState(onLongClick)
    val onPressState = rememberUpdatedState(onPress)
    val onReleaseState = rememberUpdatedState(onRelease)

    val context = LocalContext.current
    var enableState by remember { mutableStateOf(enable) }
    var lastClickTime by remember { mutableStateOf(0L) }

    val attribute = style.attribute
    val textStyle = style.attribute.textStyle

    // 创建一个可变的交互源，用于管理按钮的交互状态
    val interactionSource = state.interactionSource
    // 收集按钮是否被按下的状态
    val pressState by interactionSource.collectIsPressedAsState()

    // 根据按钮是否被按下，选择合适的容器颜色
    val containerColor = if (!enableState) {
        theme.disabledContainerColor
    } else if (pressState) {
        theme.containerColor
    } else {
        theme.containerColor
    }
    // 根据按钮是否被按下，选择合适的内容颜色
    val contentColor = if (!enableState) {
        theme.disabledContentColor
    } else if (pressState) {
        theme.contentColor
    } else{
        theme.contentColor
    }

    val containerAnimColor by animateColorAsState(
        targetValue = containerColor,
        animationSpec = animSpec.colorAnimSpec,
        label = "containerAnimColor"
    )

    val contentAnimColor by animateColorAsState(
        targetValue = contentColor,
        animationSpec = animSpec.colorAnimSpec,
        label = "contentAnimColor"
    )

    val outerScaleAnim by animateFloatAsState(
        targetValue = if ((pressState) && enableState) Constants.BUTTON_ANIM_OUTER_SCALE else 1f,
        animationSpec = animSpec.outerScaleAnimSpec,
        label = "outerScaleAnim"
    )

    val innerScaleAnim by animateFloatAsState(
        targetValue = if ((pressState) && enableState) Constants.BUTTON_ANIM_INNER_SCALE else 1f,
        animationSpec = animSpec.innerScaleAnimSpec,
        label = "innerScaleAnim"
    )

    LaunchedEffect(enable){
        enableState = enable
    }

    var isThemeChange by remember { mutableStateOf(false) }
    var isLightTheme by remember { mutableStateOf(PorscheHmiThemeUtils.isLightTheme()) }

    LaunchedEffect(theme) {
        if (!themeChangeAnimEnable) {
            isThemeChange = true
            // 延时的时间是颜色动效执行的时间
            delay(THEME_CHANGE_ANIM_DELAY_TIME * ANIM_FACTOR)
            isThemeChange = false
        }
        isLightTheme = PorscheHmiThemeUtils.isLightTheme()
    }

    PorscheTouchAreaEnhance(
        touchTargetWidth = touchTargetWidth,
        touchTargetHeight = touchTargetHeight
    ) {
        Box(
            modifier = modifier
                .semantics {
                    contentDescription = contentDesc
                }
                .then(if (widthFixed) {
                    Modifier.width(attribute.width)
                } else {
                    Modifier.widthIn(min = attribute.width)
                })
                .height(attribute.height)
                .scale(scale = outerScaleAnim)
                .background(
                    color = if (isThemeChange) containerColor else containerAnimColor,
                    shape = RoundedCornerShape(attribute.radius)
                )
                .clip(RoundedCornerShape(attribute.radius))
                .then(
                    if (toggleable) {
                        Modifier.toggleable(
                            value = toggleValue,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = enableState,
                            onValueChange = {}
                        )
                    } else {
                        Modifier
                    }
                )
                .porschePressedGesturesWithLongClick(
                    ignoreScrollGesture = pressStateIgnoreScroll,
                    interactionSource = interactionSource,
                    enabled = enableState || callbackIfNotEnable,
                    onClick = {
                        if (debounceTime == 0L) {
                            if (playSound) SoundUtil.playClickSound(context)
                            onClickState.value.invoke()
                        } else {
                            val currentTime = SystemClock.uptimeMillis()
                            if (currentTime - lastClickTime > debounceTime) {
                                lastClickTime = currentTime
                                if (playSound) SoundUtil.playClickSound(context)
                                onClickState.value.invoke()
                            }
                        }
                    },
                    onLongClick = {
                        onLongClickState.value.invoke()
                    },
                    onPress = {
                        onPressState.value.invoke()
                    },
                    onRelease = {
                        onReleaseState.value.invoke()
                    }
                )
                .porschePressedLayer(
                    pressed = pressState && enableState && pressAnimEnable,
                    lightTheme = isLightTheme,
                    lightThemeColor = pressLightThemeColor ?: Color.Black.copy(alpha = 0.1f),
                    darkThemeColor = pressDarkThemeColor
                        ?: if (ButtonKind.PRIMARY == theme.buttonKind) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = if (isThemeChange) contentColor else contentAnimColor,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                style = textStyle,
                modifier = Modifier
                    .padding(horizontal = attribute.padding)
                    .scale(scale = innerScaleAnim)
            )
        }
    }
}

/**
 * IconButton
 */
@Composable
private fun IconButton(
    modifier: Modifier = Modifier,
    image: Int,
    contentDesc: String = "",
    style: ButtonStyle,
    theme: ButtonTheme,
    animSpec: ButtonAnimSpec = ButtonAnimSpec.Default,
    themeChangeAnimEnable: Boolean = THEME_CHANGE_ANIM_DEFAULT,
    state: ButtonState,
    enable: Boolean = true,
    callbackIfNotEnable: Boolean = false,
    playSound: Boolean = PLAY_SOUND_DEFAULT,
    touchTargetWidth: Dp = style.attribute.width,
    touchTargetHeight: Dp = style.attribute.height,
    pressAnimEnable: Boolean = true,
    pressDarkThemeColor: Color? = null,
    pressLightThemeColor: Color? = null,
    debounceTime: Long = 0L,
    // 按压态是否忽略滚动手势，默认忽略
    pressStateIgnoreScroll: Boolean = true,
    toggleable: Boolean = false,
    toggleValue: Boolean = false,
    onPress: () -> Unit = {},
    onRelease: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val onClickState = rememberUpdatedState(onClick)
    val onLongClickState = rememberUpdatedState(onLongClick)
    val onPressState = rememberUpdatedState(onPress)
    val onReleaseState = rememberUpdatedState(onRelease)

    val context = LocalContext.current
    var enableState by remember { mutableStateOf(enable) }
    var lastClickTime by remember { mutableStateOf(0L) }

    val attribute = style.attribute

    // 创建一个可变的交互源，用于管理按钮的交互状态
    val interactionSource = state.interactionSource
    // 收集按钮是否被按下的状态
    val pressState by interactionSource.collectIsPressedAsState()

    // 根据按钮是否被按下，选择合适的容器颜色
    val containerColor = if (!enableState) {
        theme.disabledContainerColor
    } else if (pressState) {
        theme.containerColor
    } else {
        theme.containerColor
    }
    // 根据按钮是否被按下以及是否启用，选择合适的内容颜色
    val contentColor = if (!enableState) {
        theme.disabledContentColor
    } else if (pressState) {
        theme.contentColor
    } else {
        theme.contentColor
    }

    val containerAnimColor by animateColorAsState(
        targetValue = containerColor,
        animationSpec = animSpec.colorAnimSpec,
        label = "containerAnimColor"
    )

    val contentAnimColor by animateColorAsState(
        targetValue = contentColor,
        animationSpec = animSpec.colorAnimSpec,
        label = "contentAnimColor"
    )

    val outerScaleAnim by animateFloatAsState(
        targetValue = if ((pressState) && enableState) Constants.BUTTON_ANIM_OUTER_SCALE else 1f,
        animationSpec = animSpec.outerScaleAnimSpec,
        label = "outerScaleAnim"
    )

    val innerScaleAnim by animateFloatAsState(
        targetValue = if ((pressState) && enableState) Constants.BUTTON_ANIM_INNER_SCALE else 1f,
        animationSpec = animSpec.innerScaleAnimSpec,
        label = "innerScaleAnim"
    )

    LaunchedEffect(enable) {
        enableState = enable
    }

    var isThemeChange by remember { mutableStateOf(false) }
    var isLightTheme by remember { mutableStateOf(PorscheHmiThemeUtils.isLightTheme()) }

    LaunchedEffect(theme) {
        if (!themeChangeAnimEnable) {
            isThemeChange = true
            // 延时的时间是颜色动效执行的时间
            delay(THEME_CHANGE_ANIM_DELAY_TIME * ANIM_FACTOR)
            isThemeChange = false
        }
        isLightTheme = PorscheHmiThemeUtils.isLightTheme()
    }

    PorscheTouchAreaEnhance(
        touchTargetWidth = touchTargetWidth,
        touchTargetHeight = touchTargetHeight
    ) {
        Box(
            modifier = modifier
                .semantics {
                    contentDescription = contentDesc
                }
                .width(attribute.width)
                .height(attribute.height)
                .scale(scale = outerScaleAnim)
                .background(
                    color = if (isThemeChange) containerColor else containerAnimColor,
                    shape = RoundedCornerShape(attribute.radius)
                )
                .clip(RoundedCornerShape(attribute.radius))
                .then(
                    if (toggleable) {
                        Modifier.toggleable(
                            value = toggleValue,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = enableState,
                            onValueChange = {}
                        )
                    } else {
                        Modifier
                    }
                )
                .porschePressedGesturesWithLongClick(
                    ignoreScrollGesture = pressStateIgnoreScroll,
                    interactionSource = interactionSource,
                    enabled = enableState || callbackIfNotEnable,
                    onClick = {
                        if (debounceTime == 0L) {
                            if (playSound) SoundUtil.playClickSound(context)
                            onClickState.value.invoke()
                        } else {
                            val currentTime = SystemClock.uptimeMillis()
                            if (currentTime - lastClickTime > debounceTime) {
                                lastClickTime = currentTime
                                if (playSound) SoundUtil.playClickSound(context)
                                onClickState.value.invoke()
                            }
                        }
                    },
                    onLongClick = {
                        onLongClickState.value.invoke()
                    },
                    onPress = {
                        onPressState.value.invoke()
                    },
                    onRelease = {
                        onReleaseState.value.invoke()
                    }
                )
                .porschePressedLayer(
                    pressed = (pressState) && enableState && pressAnimEnable,
                    lightTheme = isLightTheme,
                    lightThemeColor = pressLightThemeColor ?: Color.Black.copy(alpha = 0.1f),
                    darkThemeColor = pressDarkThemeColor
                        ?: if (ButtonKind.PRIMARY == theme.buttonKind) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                colorFilter = ColorFilter.tint(if (isThemeChange) contentColor else contentAnimColor),
                modifier = Modifier
                    .width(attribute.iconWidth)
                    .height(attribute.iconHeight)
                    .scale(scale = innerScaleAnim)

            )
        }
    }
}

@Composable
private fun IconTextButton(
    modifier: Modifier = Modifier,
    text: String,
    image: Int,
    style: ButtonStyle,
    state: ButtonState,
    enable: Boolean = true,
    callbackIfNotEnable: Boolean = false,
    widthFixed: Boolean = false,
    theme: ButtonTheme,
    animSpec: ButtonAnimSpec = ButtonAnimSpec.Default,
    themeChangeAnimEnable: Boolean = THEME_CHANGE_ANIM_DEFAULT,
    playSound: Boolean = PLAY_SOUND_DEFAULT,
    touchTargetWidth: Dp = style.attribute.width,
    touchTargetHeight: Dp = style.attribute.height,
    pressAnimEnable: Boolean = true,
    pressDarkThemeColor: Color? = null,
    pressLightThemeColor: Color? = null,
    debounceTime: Long = 0L,
    // 按压态是否忽略滚动手势，默认忽略
    pressStateIgnoreScroll: Boolean = true,
    toggleable: Boolean = false,
    toggleValue: Boolean = false,
    contentDesc: String = "",
    onPress: () -> Unit = {},
    onRelease: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val onClickState = rememberUpdatedState(onClick)
    val onLongClickState = rememberUpdatedState(onLongClick)
    val onPressState = rememberUpdatedState(onPress)
    val onReleaseState = rememberUpdatedState(onRelease)

    val context = LocalContext.current
    var enableState by remember { mutableStateOf(enable) }
    var lastClickTime by remember { mutableStateOf(0L) }

    val attribute = style.attribute
    val textStyle = style.attribute.textStyle

    val interactionSource = state.interactionSource
    val pressState by interactionSource.collectIsPressedAsState()

    val containerColor = if (!enableState) {
        theme.disabledContainerColor
    } else if (pressState) {
        theme.containerColor
    } else {
        theme.containerColor
    }

    val contentColor = if (!enableState) {
        theme.disabledContentColor
    } else if (pressState) {
        theme.contentColor
    } else {
        theme.contentColor
    }

    val containerAnimColor by animateColorAsState(
        targetValue = containerColor,
        animationSpec = animSpec.colorAnimSpec,
        label = "containerAnimColor"
    )

    val contentAnimColor by animateColorAsState(
        targetValue = contentColor,
        animationSpec = animSpec.colorAnimSpec,
        label = "contentAnimColor"
    )

    val outerScaleAnim by animateFloatAsState(
        targetValue = if ((pressState) && enableState) Constants.BUTTON_ANIM_OUTER_SCALE else 1f,
        animationSpec = animSpec.outerScaleAnimSpec,
        label = "outerScaleAnim"
    )

    val innerScaleAnim by animateFloatAsState(
        targetValue = if ((pressState) && enableState) Constants.BUTTON_ANIM_INNER_SCALE else 1f,
        animationSpec = animSpec.innerScaleAnimSpec,
        label = "innerScaleAnim"
    )

    LaunchedEffect(enable) {
        enableState = enable
    }

    var isThemeChange by remember { mutableStateOf(false) }
    var isLightTheme by remember { mutableStateOf(PorscheHmiThemeUtils.isLightTheme()) }

    LaunchedEffect(theme) {
        if (!themeChangeAnimEnable) {
            isThemeChange = true
            // 延时的时间是颜色动效执行的时间
            delay(THEME_CHANGE_ANIM_DELAY_TIME * ANIM_FACTOR)
            isThemeChange = false
        }
        isLightTheme = PorscheHmiThemeUtils.isLightTheme()
    }

    PorscheTouchAreaEnhance(
        touchTargetWidth = touchTargetWidth,
        touchTargetHeight = touchTargetHeight
    ) {
        Box(
            modifier = modifier
                .semantics {
                    contentDescription = contentDesc
                }
                .wrapContentSize()
                .then(if (widthFixed) {
                    Modifier.width(attribute.width)
                } else {
                    Modifier.widthIn(min = attribute.width)
                })
                .height(attribute.height)
                .scale(scale = outerScaleAnim)
                .background(
                    color = if (isThemeChange) containerColor else containerAnimColor,
                    shape = RoundedCornerShape(attribute.radius)
                )
                .clip(RoundedCornerShape(attribute.radius))
                .then(
                    if (toggleable) {
                        Modifier.toggleable(
                            value = toggleValue,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = enableState,
                            onValueChange = {}
                        )
                    } else {
                        Modifier
                    }
                )
                .porschePressedGesturesWithLongClick(
                    ignoreScrollGesture = pressStateIgnoreScroll,
                    interactionSource = interactionSource,
                    enabled = enableState || callbackIfNotEnable,
                    onClick = {
                        if (debounceTime == 0L) {
                            if (playSound) SoundUtil.playClickSound(context)
                            onClickState.value.invoke()
                        } else {
                            val currentTime = SystemClock.uptimeMillis()
                            if (currentTime - lastClickTime > debounceTime) {
                                lastClickTime = currentTime
                                if (playSound) SoundUtil.playClickSound(context)
                                onClickState.value.invoke()
                            }
                        }
                    },
                    onLongClick = {
                        onLongClickState.value.invoke()
                    },
                    onPress = {
                        onPressState.value.invoke()
                    },
                    onRelease = {
                        onReleaseState.value.invoke()
                    }
                )
                .porschePressedLayer(
                    pressed = (pressState) && enableState && pressAnimEnable,
                    lightTheme = isLightTheme,
                    lightThemeColor = pressLightThemeColor ?: Color.Black.copy(alpha = 0.1f),
                    darkThemeColor = pressDarkThemeColor
                        ?: if (ButtonKind.PRIMARY == theme.buttonKind) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            IconTextButtonInner(
                scaleAnim = innerScaleAnim,
                contentAnimColor = contentAnimColor,
                attribute = attribute,
                image = image,
                text = text,
                contentColor = contentColor,
                textStyle = textStyle,
                isThemeChange = isThemeChange
            )
        }
    }
}

@Composable
private fun IconTextButtonInner(
    modifier: Modifier = Modifier,
    scaleAnim: Float,
    contentAnimColor: Color,
    attribute: ButtonAttribute,
    image: Int,
    text: String,
    contentColor: Color,
    textStyle: TextStyle,
    isThemeChange: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .wrapContentSize()
            .scale(scale = scaleAnim)
            .padding(horizontal = attribute.padding)
    ) {
        Image(
            modifier = Modifier.width(attribute.iconWidth).height(attribute.iconHeight),
            painter = painterResource(image),
            contentDescription = "",
            colorFilter = ColorFilter.tint(if (isThemeChange) contentColor else contentAnimColor),
        )
        Text(
            text = text,
            color = if (isThemeChange) contentColor else contentAnimColor,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.spacing_x_small)),
            style = textStyle
        )
    }
}

/**
 * IconButton
 */
@Composable
private fun CustomContentButton(
    modifier: Modifier = Modifier,
    style: ButtonStyle,
    theme: ButtonTheme,
    animSpec: ButtonAnimSpec = ButtonAnimSpec.Default,
    themeChangeAnimEnable: Boolean = THEME_CHANGE_ANIM_DEFAULT,
    state: ButtonState,
    enable: Boolean = true,
    callbackIfNotEnable: Boolean = false,
    playSound: Boolean = PLAY_SOUND_DEFAULT,
    touchTargetWidth: Dp = style.attribute.width,
    touchTargetHeight: Dp = style.attribute.height,
    pressAnimEnable: Boolean = true,
    pressDarkThemeColor: Color? = null,
    pressLightThemeColor: Color? = null,
    widthFixed: Boolean = true,
    debounceTime: Long = 0L,
    // 按压态是否忽略滚动手势，默认忽略
    pressStateIgnoreScroll: Boolean = true,
    toggleable: Boolean = false,
    toggleValue: Boolean = false,
    customContent: @Composable () -> Unit,
    onPress: () -> Unit = {},
    onRelease: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val onClickState = rememberUpdatedState(onClick)
    val onLongClickState = rememberUpdatedState(onLongClick)
    val onPressState = rememberUpdatedState(onPress)
    val onReleaseState = rememberUpdatedState(onRelease)

    val context = LocalContext.current
    var enableState by remember { mutableStateOf(enable) }
    var lastClickTime by remember { mutableStateOf(0L) }

    val attribute = style.attribute

    // 创建一个可变的交互源，用于管理按钮的交互状态
    val interactionSource = state.interactionSource
    // 收集按钮是否被按下的状态
    val pressState by interactionSource.collectIsPressedAsState()

    // 根据按钮是否被按下，选择合适的容器颜色
    val containerColor = if (!enableState) {
        theme.disabledContainerColor
    } else if (pressState) {
        theme.containerColor
    } else {
        theme.containerColor
    }
    // 根据按钮是否被按下以及是否启用，选择合适的内容颜色
    val contentColor = if (!enableState) {
        theme.disabledContentColor
    } else if (pressState) {
        theme.contentColor
    } else {
        theme.contentColor
    }

    val containerAnimColor by animateColorAsState(
        targetValue = containerColor,
        animationSpec = animSpec.colorAnimSpec,
        label = "containerAnimColor"
    )

    val contentAnimColor by animateColorAsState(
        targetValue = contentColor,
        animationSpec = animSpec.colorAnimSpec,
        label = "contentAnimColor"
    )

    val outerScaleAnim by animateFloatAsState(
        targetValue = if ((pressState) && enableState) Constants.BUTTON_ANIM_OUTER_SCALE else 1f,
        animationSpec = animSpec.outerScaleAnimSpec,
        label = "outerScaleAnim"
    )

    val innerScaleAnim by animateFloatAsState(
        targetValue = if ((pressState) && enableState) Constants.BUTTON_ANIM_INNER_SCALE else 1f,
        animationSpec = animSpec.innerScaleAnimSpec,
        label = "innerScaleAnim"
    )

    LaunchedEffect(enable) {
        enableState = enable
    }

    var isThemeChange by remember { mutableStateOf(false) }
    var isLightTheme by remember { mutableStateOf(PorscheHmiThemeUtils.isLightTheme()) }

    LaunchedEffect(theme) {
        if (!themeChangeAnimEnable) {
            isThemeChange = true
            // 延时的时间是颜色动效执行的时间
            delay(THEME_CHANGE_ANIM_DELAY_TIME * ANIM_FACTOR)
            isThemeChange = false
        }
        isLightTheme = PorscheHmiThemeUtils.isLightTheme()
    }

    PorscheTouchAreaEnhance(
        touchTargetWidth = touchTargetWidth,
        touchTargetHeight = touchTargetHeight
    ) {
        Box(
            modifier = modifier
                .then(if (widthFixed) {
                    Modifier.width(attribute.width)
                } else {
                    Modifier.widthIn(min = attribute.width)
                })
                .height(attribute.height)
                .scale(scale = outerScaleAnim)
                .background(
                    color = if (isThemeChange) containerColor else containerAnimColor,
                    shape = RoundedCornerShape(attribute.radius)
                )
                .clip(RoundedCornerShape(attribute.radius))
                .then(
                    if (toggleable) {
                        Modifier.toggleable(
                            value = toggleValue,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = enableState,
                            onValueChange = {}
                        )
                    } else {
                        Modifier
                    }
                )
                .porschePressedGesturesWithLongClick(
                    ignoreScrollGesture = pressStateIgnoreScroll,
                    interactionSource = interactionSource,
                    enabled = enableState || callbackIfNotEnable,
                    onClick = {
                        if (debounceTime == 0L) {
                            if (playSound) SoundUtil.playClickSound(context)
                            onClickState.value.invoke()
                        } else {
                            val currentTime = SystemClock.uptimeMillis()
                            if (currentTime - lastClickTime > debounceTime) {
                                lastClickTime = currentTime
                                if (playSound) SoundUtil.playClickSound(context)
                                onClickState.value.invoke()
                            }
                        }
                    },
                    onLongClick = {
                        onLongClickState.value.invoke()
                    },
                    onPress = {
                        onPressState.value.invoke()
                    },
                    onRelease = {
                        onReleaseState.value.invoke()
                    }
                )
                .porschePressedLayer(
                    pressed = pressState && enableState && pressAnimEnable,
                    lightTheme = isLightTheme,
                    lightThemeColor = pressLightThemeColor ?: Color.Black.copy(alpha = 0.1f),
                    darkThemeColor = pressDarkThemeColor
                        ?: if (ButtonKind.PRIMARY == theme.buttonKind) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier.scale(scale = innerScaleAnim)
            ) {
                customContent()
            }
        }
    }
}
