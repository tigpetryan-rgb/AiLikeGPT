# Local models

Place local model weights in this directory.

The default configuration expects:

```text
models/model.gguf
```

GGUF and other large model binaries are ignored by Git. They remain on the local machine and are loaded directly by the runtime.

For a packaged offline release, this directory can be populated by the installer so the application never needs to download a model at runtime.
