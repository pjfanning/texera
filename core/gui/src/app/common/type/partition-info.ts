export interface PartitionInfo {
  type: string;
  satisfies(other: PartitionInfo | null): boolean;
  merge(other: PartitionInfo | null): PartitionInfo;
}

export interface HashPartition extends PartitionInfo {
  type: "hash";
  hashAttributeNames: string[];
}

export interface RangePartition extends PartitionInfo {
  type: "range";
  rangeAttributeNames: string[];
  rangeMin: number;
  rangeMax: number;
}

export interface SinglePartition extends PartitionInfo {
  type: "single";
}

export interface BroadcastPartition extends PartitionInfo {
  type: "broadcast";
}

export interface UnknownPartition extends PartitionInfo {
  type: "none";
}

// TypeScript union type for all PartitionInfo types
export type PartitionInfoUnion =
  | HashPartition
  | RangePartition
  | SinglePartition
  | BroadcastPartition
  | UnknownPartition;
