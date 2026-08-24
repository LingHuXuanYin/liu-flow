# 截图占位

启动 App 后，把每个屏的截图放到本目录，文件名按下面命名（覆盖空文件即可）：

| 文件名                | 对应屏       |
| --------------------- | ------------ |
| `focus.png`           | 主屏（准备）  |
| `running.png`         | 专注中        |
| `rest.png`            | 休息          |
| `history.png`         | 记录          |
| `stats.png`           | 统计          |
| `heatmap.png`         | 时段热力图   |
| `weekly.png`          | 周报          |
| `me.png`              | 我的          |
| `settings.png`        | 设置          |

## 截图方法

### 模拟器
Extended Controls 面板 → 相机图标 → 截图直接保存到电脑。
或者 `adb shell screencap -p /sdcard/x.png && adb pull /sdcard/x.png .`。

### 真机
Android 13+：设置 → 截屏。
或者 `adb shell screencap -p /sdcard/x.png && adb pull /sdcard/x.png .`。
