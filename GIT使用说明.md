# 本地版本管理

仓库位置：桌面／随日项目／源码／SuiRi。
首次存档：saber喵 1.1.5，分支 main，标签 v1.1.5。
GitHub 公开仓库：https://github.com/myh051011-glitch/saber-miao
远程名称 origin，main 跟踪 origin/main；完整本地提交历史与 v1.1.5 标签已上传。
版本下载：https://github.com/myh051011-glitch/saber-miao/releases/tag/v1.1.5
历史版本的问题与改动见 docs/versions/README.md；旧版APK和源码归档仍保存在桌面版本归档目录。

在源码目录打开终端：

```powershell
git status
git diff
git log --oneline --decorate
```

每次改动验证后存档：

```powershell
git add .
git commit -m "描述本次修改"
```

首次提交使用本仓库专用作者 Local Developer <local@localhost>。
需要时用 git config user.name 和 git config user.email 设置后续提交作者。

APK、构建缓存、API配置、个人数据库与签名私钥不入库。
已有签名密钥仍在工作目录 work/suiri-build/personal-debug.p12，应单独妥善备份；重新生成密钥会影响覆盖升级。
此标签记录当前源码，不代表重新进行了手机验收。

本地提交后，运行 `git push` 上传源码；新版本标签使用 `git push origin 标签名`。签名私钥和API凭证仍不入库。
