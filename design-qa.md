# 1.0 原生 Android 视觉验收

final result: blocked

参考图：`../design-1.0/assistant-selected-navy.png`，用户确认图3排版与图2深蓝金色配色，底部弹窗仅显示当前回复。

已实现：原生深色页面、蓝金控件、浮动头像、底部台词层、表情区域、天气事实条、输入栏、固定菜单、弹窗关闭后保留页面滚动位置。修复小组件旧浅色背景与新浅色文字不匹配的问题。

无法完成渲染比较：本环境的 ADB 在用户目录初始化时报 `Cannot mkdir '\.android': Permission denied`，SDK 中没有已安装的 Android 模拟器系统镜像。未获得真机截图，不能声称原生渲染与效果图对照验收通过。

已知实现与视觉稿差别：角色直接使用用户九宫格图片的区域，保留彩色底图，不是透明立绘或 Live2D；无生成稿的背景插画和复杂金色花纹。主要排版与配色已实现，但像素级一致性尚未验证。

待手机验证：

1. 原应用覆盖升级，原课表、待办、令牌、聊天、记忆仍在。
2. 非助手页面可拖动头像；关闭台词层后保持原页面与滚动位置。
3. 弹窗仅显示一段回复；键盘出现时角色与天气条收起，输入和发送可见。
4. 九种表情预览与模型返回的情绪切换；完整助手页历史记录可读。
5. 4×5 小组件天气、重点提醒、课程与待办滚动显示，字号适配 One UI。
6. 使用用户自己的和风凭据实测：成功、断网、缓存、雨天、无预警和预警详情。
7. 通知授权、三星后台调度、锁屏恢复后的天气与 Canvas 同步。

构建、签名与 JVM 检查属于代码验证，不替代以上视觉和真机检查。APK 可用于后续真机验收，不将此报告写成通过。

## 1.1 additional acceptance

Native device verification remains blocked. Check manual full timetable and week selection, month without courses, today course state boundaries, widget blank tap and Saber avatar, forecast day/night warnings. APK metadata, signing and JVM checks are separate from these device checks.

## 1.1.2 acceptance pending

Check rounded dialogs and date/time pickers, collapsed settings, task validation and reminder toggle, 42-cell month widget at 4x5 and resized dimensions, day-cell deep links, ended class filtering, edited avatar and tired expression on the Samsung device. Build and JVM tests do not establish UI acceptance.

## 1.1.3 acceptance pending

Verify new launcher name/icon, location permission deny/approximate/precise, GPS disabled, moving Shanghai to another region and back, failed GeoAPI, last-location label, foreground-only updates, independent city cache and budget, assistant Settings/History, exported PPTX in PowerPoint/WPS. JVM and package checks are not device acceptance.
