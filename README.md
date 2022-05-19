# Floating Camera Preview

Floating Camera Preview simply create a working window with the camera.

## Installation

Setting Maven.

```bash
maven { url 'https://jitpack.io' }
```

Implementation Version.

```bash
implementation 'com.github.kekhong95:floating-camera-preview:1.1'
```

## Usage

```python
CameraWindow window = new CameraWindow(this, new CameraCustomView.CameraCustomViewListener() {
            @Override
            public void onClose() {
                finish();
            }

            @Override
            public void onError(Exception exception) {

            }
        });
window.create();
// to close call
window.close();
```

## Preview
![Alt Text](https://github.com/kekhong95/floating-camera-preview/blob/main/ezgif.com-gif-maker.gif)

## License
[MIT](https://choosealicense.com/licenses/mit/)
