# Toy SAM / BAM Files
This directory contains valid tiny files for testing.

* `toy.sam` comes from the [samtools repository](https://github.com/samtools/samtools/blob/554bb9043a2da9ea5db85095dce21b742ba1fb71/examples/toy.sam).
* `toy-rg.sam` is `toy.sam` with a manually-added `@RG` header line.
* `toy.bam` and `toy-rg.bam` come from running `samtools view -bS XXX.sam > XXX.bam`.
