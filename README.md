![Maven Central: Time Picker](https://img.shields.io/maven-central/v/io.github.dongchyeon/time-picker?color=orange&label=Maven%20Central%20%3A%20Time%20Picker)
![Minimum SDK: 24](https://img.shields.io/badge/Minimum%20SDK-24-yellowgreen)

## Time Picker - A customizable Jetpack Compose time picker.

![Main Image](https://github.com/user-attachments/assets/033e6afc-d8bf-4492-9d73-57aa37e7db66)

A fully customizable time picker component for Jetpack Compose.<br>
It supports both 12-hour and 24-hour formats.<br>
This library allows you to configure various visual aspects like the style of hours, minutes, and AM/PM labels.<br>
It also provides full localization support and event handling for custom behaviors.

여러 커스터마이징 요소를 제공하는 Jetpack Compose를 이용한 TimePicker 컴포넌트입니다.

12-hour Format Time Picker              |  24-hour Format Time Picker
:-------------------------:|:-------------------------:
![12-hour](https://github.com/user-attachments/assets/4a83ac1b-6a55-495a-9b0b-4dfebd964085)  |  ![24-hour](https://github.com/user-attachments/assets/5e1d34f3-2327-4157-872d-56210fabe391)

## Features

#### 1. Customizable Styling
- **AM/PM Configuration**: Customize AM/PM label text style, color, and layout.
- **Time Label Styling**: Adjust hour and minute text styles, colors, and item spacing via `PickerStyle`.
- **Picker Selector Configuration**: Modify the appearance of the picker selector, including background color, shape, and border.
- **Curve Effect**: Apply a curve effect to the picker list for a 3D cylindrical visual. Options include alpha fading and vertical scaling.
- **Visible Items Count**: Control the number of visible items for compact or expansive display.

#### 2. Localization Support
- Supports both English (EN) and Korean (KO) locales.
- Dynamically formats hours, minutes, and AM/PM labels:
  - EN: “PM 12:30”
  - KO: “오후 12:30”

#### 3. Event Handling
- React to time changes using `onValueChange` to update the selected time.
- Implement custom behaviors on value change or selection.

#### 4. Layouts
- 12-hour Format Time Picker: Ideal for use in regions where 12-hour format is preferred.
- 24-hour Format Time Picker: Useful in regions where 24-hour time format is standard.

## Getting Started

Add the following to your build.gradle file:

```gradle
// Project level build.gradle
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// App level build.gradle
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    implementation("io.github.dongchyeon:time-picker:<latest_version>")
}
```

## Usage

#### 12-hour Format Time Picker Example

```kotlin
var selectedTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time) }

TimePicker(
    initialTime = selectedTime,
    timeFormat = TimeFormat.TWELVE_HOUR
) { newTime ->
    selectedTime = newTime
}
```

#### 24-hour Format Time Picker Example

```kotlin
var selectedTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time) }
                        
TimePicker(
    initialTime = selectedTime,
    timeFormat = TimeFormat.TWENTY_FOUR_HOUR
) { newTime ->
    selectedTime = newTime
}
```

### Customized Options

#### 1. Picker Style Configuration

PickerStyle allows you to customize text styles, colors, and item spacing.

```kotlin
TimePicker(
    // ...
    style = TimePickerDefaults.pickerStyle(
        textStyle = MaterialTheme.typography.bodyLarge,
        textColor = Color.White,
        itemSpacing = 12.dp
    ),
    // ...
)
```

#### 2. AM/PM Locale Configuraiton

Configure the time picker to display AM/PM labels in either English or Korean.

```kotlin
TimePicker(
    // ...
    timeFormat = TimeFormat.TWELVE_HOUR
    // ...
)

TimePicker(
    // ...
    timeFormat = TimeFormat.TWELVE_HOUR_KOREAN
    // ...
)
```

#### 3. Picker Selector Configuration

Modify selector color, shape, and border.

```kotlin
TimePicker(
    // ...
    selector = TimePickerDefaults.pickerSelector(
        color = Color.Gray.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Gray)
    )
    // ...
)
```

#### 4. Visible Items Count Configuration

Control how many items are shown in the picker at once.

```kotlin
TimePicker(
    // ...
    visibleItemsCount = 7
    // ...
)
```

#### 5. Curve Effect Configuration

Apply a curve effect to the picker for a 3D cylindrical visual.
Options include:
- Enable/disable alpha fading (alphaEnabled, minAlpha)
- Enable/disable vertical scaling (scaleYEnabled, minScaleY)

```kotlin
TimePicker(
    // ...
    curveEffect = TimePickerDefaults.curveEffect(
        alphaEnabled = true,
        minAlpha = 0.3f,
        scaleYEnabled = true,
        minScaleY = 0.85f
    )
    // ...
)
```

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

### Contributions, bug reports, and feature requests are welcome!
