# AGENTS.md — drawio2tikz

## What this is

Java 8+ Maven project that converts **draw.io** (`.drawio` XML) diagrams into **TikZ** LaTeX code. Single package `edu.fudan.drawio2tikz`, no external runtime deps beyond the JDK.

## Entrypoints

- **CLI:** `Drawio2Tikz.main` — takes exactly 2 args: `<input_file> <output_file>`. Produces a standalone compilable LaTeX doc.
- **Batch gallery:** `Gallery.java` (no package, at project root) — walks `resources/*.drawio`, generates `.tex` files, optionally runs `pdflatex` to build `resources/gallery.pdf`.

## Build & test

```sh
mvn test                         # JUnit 4 tests
mvn test -Dtest=TestPoint        # single test class
```

No Maven wrapper — requires system `mvn`. No CI config in repo.

## Code style

`.clang-format` (LLVM style, indent 4, col limit 120). Run formatting via:

```sh
sh src/test/resources/format.sh   # formats all .java files including Gallery.java
```

## Architecture

`TikzGen.fromFile()` → DOM parse → `GeometryFactory.createGeometry()` per `<mxCell>` → `Line` or `Shape` → `generateTikz()` / `generateDoc()`.

Key quirks:
- Y axis is negated on output (draw.io = downward-positive; TikZ = upward-positive). `SCALE_FACTOR = 1/40` converts draw.io to TikZ units.
- Rotation in `Point.rotateBy()` is clockwise; the angle is negated during shape coordinate output to compensate for the Y flip.
- Colors get `\definecolor{<uniqueName>}{HTML}{<hex>}` where `uniqueName()` prepends `"C"` to the hex string to avoid LaTeX macro collisions.
- Only 3 shapes supported: `RECTANGLE`, `ELLIPSE`, `TRIANGLE`. Ellipse rotation prints a warning and is skipped.
- Gradients map to TikZ's 8-direction shading library; angle correction is applied for rotated shapes.

## Test details

- JUnit 4 (`junit:junit:4.13.2`, test scope only).
- 3 test files: `TestColor`, `TestPoint`, `TestGeometryFactory`.
- No integration tests (no draw.io fixture files are loaded by tests).

## Resources

`resources/` contains fixture `.drawio` files, golden `.tex` outputs, and raster `.png` comparison images used by `Gallery.java`. The gallery LaTeX doc (`gallery.tex`) is generated, not hand-written — treat it as build output.

## VS Code

`.vscode/settings.json` associates `*.drawio` with XML language mode and `Maven` explorer view set to hierarchical.
