GostVPN
=============

Um simples aplicativo para executar o [Gost](https://github.com/ginuerzh/gost) no android.

## Note
Incomplete app, feel free to implement the VPN part.

## Build instructions
- Install sdk
- Clone this repository
- Open and build in Intellij or other IDE

## Contributing
If you would like to contribute, please keep in mind the following rules:
- Try to stick to the project's existing code style and naming conventions
- Our preferred tech stack is Kotlin, MVVM, data-binding and coroutines, so any new features or large refactors should conform to this preferred tech stack

By making a contribution to this project you agree to the following:

1. I assign any and all copyright related to the contribution to SlipkProjects;
2. I certify that the contribution was created in whole by me;
3. I understand and agree that this project and the contribution are public and that a record of the contribution (including all personal information I submit with it) is maintained indefinitely and may be redistributed with this project or the open source license(s) involved.

### Code Sources
* `service/src/main/jniLibs/armeabi-v7a/libgost.so`:
    * https://github.com/ginuerzh/gost/releases/download/v2.11.2/gost-linux-armv7-2.11.2.gz

### License
```
MIT License

Copyright (c) 2022 SlipkProjects

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```