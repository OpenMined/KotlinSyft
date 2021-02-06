[syft](../index.md) / [org.openmined.syft.data.samplers](./index.md)

## Package org.openmined.syft.data.samplers

### Types

| Name | Summary |
|---|---|
| [BatchSampler](-batch-sampler/index.md) | Wraps another sampler to yield a mini-batch of indices.`class BatchSampler : `[`Sampler`](-sampler/index.md) |
| [RandomSampler](-random-sampler/index.md) | Samples elements randomly. If without replacement, then sample from a shuffled dataset. If with replacement, then user can specify :attr:`num_samples` to draw.`class RandomSampler : `[`Sampler`](-sampler/index.md) |
| [Sampler](-sampler/index.md) | Base class for all Samplers. Every Sampler subclass has to provide an :method:`indices` method, providing a way to iterate over indices of dataset elements, and a :method:`length` method that returns the length of the returned iterators.`interface Sampler` |
| [SequentialSampler](-sequential-sampler/index.md) | Samples elements sequentially, always in the same order.`class SequentialSampler : `[`Sampler`](-sampler/index.md) |
