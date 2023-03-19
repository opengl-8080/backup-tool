# Backup Tool

## 必須環境

- Java 17
- Windows

## ビルド

```
$ gradlew jar
```

## 実行方法

- プロパティファイルを用意する

```properties
# キャッシュやログファイルの出力先となるフォルダ
# 未指定の場合は実行時のワーキングフォルダになる
home=path/to/backup-home

# バックアップ対象のフォルダの定義
# origin.XXX と destination.XXX の2つをセットで定義する
# XXX の部分はセットを識別する任意の名前(Windows のファイル名・フォルダ名に使える文字にする必要がある)
# origin はバックアップ対象のフォルダのパス
# destination はバックアップ先のフォルダのパス
# パス区切り文字はスラッシュ (/) にする必要がある
# origin, destination のセットは複数定義できる
origin.foo=path/to/foo/origin/dir
destination.foo=path/to/foo/destination/dir
```

- 上で作ったプロパティファイルを引数にして実行する

```
$ java -jar backup-tool.jar --config=path/to/config.properites
```

## 更新内容

- 1.0.1
  - バックアップ先フォルダがない場合にエラーになる不具合を修正
- 1.0.0
  - 初回リリース
