// Code generated by protoc-gen-ts_proto. DO NOT EDIT.
// versions:
//   protoc-gen-ts_proto  v2.2.0
//   protoc               v5.28.0
// source: edu/uci/ics/amber/engine/architecture/worker/controlreturns.proto

/* eslint-disable */
import { BinaryReader, BinaryWriter } from "@bufbuild/protobuf/wire";
import { WorkerMetrics, WorkerState, workerStateFromJSON, workerStateToJSON } from "./statistics";

export const protobufPackage = "edu.uci.ics.amber.engine.architecture.worker";

export interface CurrentInputTupleInfo {
}

export interface ControlException {
  msg: string;
}

export interface TypedValue {
  expression: string;
  valueRef: string;
  valueStr: string;
  valueType: string;
  expandable: boolean;
}

export interface EvaluatedValue {
  value: TypedValue | undefined;
  attributes: TypedValue[];
}

export interface ControlReturnV2 {
  controlException?: ControlException | undefined;
  workerState?: WorkerState | undefined;
  workerMetrics?: WorkerMetrics | undefined;
  currentInputTupleInfo?: CurrentInputTupleInfo | undefined;
  evaluatedValue?: EvaluatedValue | undefined;
}

function createBaseCurrentInputTupleInfo(): CurrentInputTupleInfo {
  return {};
}

export const CurrentInputTupleInfo: MessageFns<CurrentInputTupleInfo> = {
  encode(_: CurrentInputTupleInfo, writer: BinaryWriter = new BinaryWriter()): BinaryWriter {
    return writer;
  },

  decode(input: BinaryReader | Uint8Array, length?: number): CurrentInputTupleInfo {
    const reader = input instanceof BinaryReader ? input : new BinaryReader(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseCurrentInputTupleInfo();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skip(tag & 7);
    }
    return message;
  },

  fromJSON(_: any): CurrentInputTupleInfo {
    return {};
  },

  toJSON(_: CurrentInputTupleInfo): unknown {
    const obj: any = {};
    return obj;
  },

  create<I extends Exact<DeepPartial<CurrentInputTupleInfo>, I>>(base?: I): CurrentInputTupleInfo {
    return CurrentInputTupleInfo.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<CurrentInputTupleInfo>, I>>(_: I): CurrentInputTupleInfo {
    const message = createBaseCurrentInputTupleInfo();
    return message;
  },
};

function createBaseControlException(): ControlException {
  return { msg: "" };
}

export const ControlException: MessageFns<ControlException> = {
  encode(message: ControlException, writer: BinaryWriter = new BinaryWriter()): BinaryWriter {
    if (message.msg !== "") {
      writer.uint32(10).string(message.msg);
    }
    return writer;
  },

  decode(input: BinaryReader | Uint8Array, length?: number): ControlException {
    const reader = input instanceof BinaryReader ? input : new BinaryReader(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseControlException();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.msg = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skip(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ControlException {
    return { msg: isSet(object.msg) ? globalThis.String(object.msg) : "" };
  },

  toJSON(message: ControlException): unknown {
    const obj: any = {};
    if (message.msg !== "") {
      obj.msg = message.msg;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ControlException>, I>>(base?: I): ControlException {
    return ControlException.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ControlException>, I>>(object: I): ControlException {
    const message = createBaseControlException();
    message.msg = object.msg ?? "";
    return message;
  },
};

function createBaseTypedValue(): TypedValue {
  return { expression: "", valueRef: "", valueStr: "", valueType: "", expandable: false };
}

export const TypedValue: MessageFns<TypedValue> = {
  encode(message: TypedValue, writer: BinaryWriter = new BinaryWriter()): BinaryWriter {
    if (message.expression !== "") {
      writer.uint32(10).string(message.expression);
    }
    if (message.valueRef !== "") {
      writer.uint32(18).string(message.valueRef);
    }
    if (message.valueStr !== "") {
      writer.uint32(26).string(message.valueStr);
    }
    if (message.valueType !== "") {
      writer.uint32(34).string(message.valueType);
    }
    if (message.expandable !== false) {
      writer.uint32(40).bool(message.expandable);
    }
    return writer;
  },

  decode(input: BinaryReader | Uint8Array, length?: number): TypedValue {
    const reader = input instanceof BinaryReader ? input : new BinaryReader(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTypedValue();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.expression = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.valueRef = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.valueStr = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.valueType = reader.string();
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.expandable = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skip(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): TypedValue {
    return {
      expression: isSet(object.expression) ? globalThis.String(object.expression) : "",
      valueRef: isSet(object.valueRef) ? globalThis.String(object.valueRef) : "",
      valueStr: isSet(object.valueStr) ? globalThis.String(object.valueStr) : "",
      valueType: isSet(object.valueType) ? globalThis.String(object.valueType) : "",
      expandable: isSet(object.expandable) ? globalThis.Boolean(object.expandable) : false,
    };
  },

  toJSON(message: TypedValue): unknown {
    const obj: any = {};
    if (message.expression !== "") {
      obj.expression = message.expression;
    }
    if (message.valueRef !== "") {
      obj.valueRef = message.valueRef;
    }
    if (message.valueStr !== "") {
      obj.valueStr = message.valueStr;
    }
    if (message.valueType !== "") {
      obj.valueType = message.valueType;
    }
    if (message.expandable !== false) {
      obj.expandable = message.expandable;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TypedValue>, I>>(base?: I): TypedValue {
    return TypedValue.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TypedValue>, I>>(object: I): TypedValue {
    const message = createBaseTypedValue();
    message.expression = object.expression ?? "";
    message.valueRef = object.valueRef ?? "";
    message.valueStr = object.valueStr ?? "";
    message.valueType = object.valueType ?? "";
    message.expandable = object.expandable ?? false;
    return message;
  },
};

function createBaseEvaluatedValue(): EvaluatedValue {
  return { value: undefined, attributes: [] };
}

export const EvaluatedValue: MessageFns<EvaluatedValue> = {
  encode(message: EvaluatedValue, writer: BinaryWriter = new BinaryWriter()): BinaryWriter {
    if (message.value !== undefined) {
      TypedValue.encode(message.value, writer.uint32(10).fork()).join();
    }
    for (const v of message.attributes) {
      TypedValue.encode(v!, writer.uint32(18).fork()).join();
    }
    return writer;
  },

  decode(input: BinaryReader | Uint8Array, length?: number): EvaluatedValue {
    const reader = input instanceof BinaryReader ? input : new BinaryReader(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEvaluatedValue();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.value = TypedValue.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.attributes.push(TypedValue.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skip(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): EvaluatedValue {
    return {
      value: isSet(object.value) ? TypedValue.fromJSON(object.value) : undefined,
      attributes: globalThis.Array.isArray(object?.attributes)
        ? object.attributes.map((e: any) => TypedValue.fromJSON(e))
        : [],
    };
  },

  toJSON(message: EvaluatedValue): unknown {
    const obj: any = {};
    if (message.value !== undefined) {
      obj.value = TypedValue.toJSON(message.value);
    }
    if (message.attributes?.length) {
      obj.attributes = message.attributes.map((e) => TypedValue.toJSON(e));
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<EvaluatedValue>, I>>(base?: I): EvaluatedValue {
    return EvaluatedValue.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<EvaluatedValue>, I>>(object: I): EvaluatedValue {
    const message = createBaseEvaluatedValue();
    message.value = (object.value !== undefined && object.value !== null)
      ? TypedValue.fromPartial(object.value)
      : undefined;
    message.attributes = object.attributes?.map((e) => TypedValue.fromPartial(e)) || [];
    return message;
  },
};

function createBaseControlReturnV2(): ControlReturnV2 {
  return {
    controlException: undefined,
    workerState: undefined,
    workerMetrics: undefined,
    currentInputTupleInfo: undefined,
    evaluatedValue: undefined,
  };
}

export const ControlReturnV2: MessageFns<ControlReturnV2> = {
  encode(message: ControlReturnV2, writer: BinaryWriter = new BinaryWriter()): BinaryWriter {
    if (message.controlException !== undefined) {
      ControlException.encode(message.controlException, writer.uint32(10).fork()).join();
    }
    if (message.workerState !== undefined) {
      writer.uint32(16).int32(message.workerState);
    }
    if (message.workerMetrics !== undefined) {
      WorkerMetrics.encode(message.workerMetrics, writer.uint32(26).fork()).join();
    }
    if (message.currentInputTupleInfo !== undefined) {
      CurrentInputTupleInfo.encode(message.currentInputTupleInfo, writer.uint32(34).fork()).join();
    }
    if (message.evaluatedValue !== undefined) {
      EvaluatedValue.encode(message.evaluatedValue, writer.uint32(42).fork()).join();
    }
    return writer;
  },

  decode(input: BinaryReader | Uint8Array, length?: number): ControlReturnV2 {
    const reader = input instanceof BinaryReader ? input : new BinaryReader(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseControlReturnV2();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.controlException = ControlException.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.workerState = reader.int32() as any;
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.workerMetrics = WorkerMetrics.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.currentInputTupleInfo = CurrentInputTupleInfo.decode(reader, reader.uint32());
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.evaluatedValue = EvaluatedValue.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skip(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ControlReturnV2 {
    return {
      controlException: isSet(object.controlException) ? ControlException.fromJSON(object.controlException) : undefined,
      workerState: isSet(object.workerState) ? workerStateFromJSON(object.workerState) : undefined,
      workerMetrics: isSet(object.workerMetrics) ? WorkerMetrics.fromJSON(object.workerMetrics) : undefined,
      currentInputTupleInfo: isSet(object.currentInputTupleInfo)
        ? CurrentInputTupleInfo.fromJSON(object.currentInputTupleInfo)
        : undefined,
      evaluatedValue: isSet(object.evaluatedValue) ? EvaluatedValue.fromJSON(object.evaluatedValue) : undefined,
    };
  },

  toJSON(message: ControlReturnV2): unknown {
    const obj: any = {};
    if (message.controlException !== undefined) {
      obj.controlException = ControlException.toJSON(message.controlException);
    }
    if (message.workerState !== undefined) {
      obj.workerState = workerStateToJSON(message.workerState);
    }
    if (message.workerMetrics !== undefined) {
      obj.workerMetrics = WorkerMetrics.toJSON(message.workerMetrics);
    }
    if (message.currentInputTupleInfo !== undefined) {
      obj.currentInputTupleInfo = CurrentInputTupleInfo.toJSON(message.currentInputTupleInfo);
    }
    if (message.evaluatedValue !== undefined) {
      obj.evaluatedValue = EvaluatedValue.toJSON(message.evaluatedValue);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ControlReturnV2>, I>>(base?: I): ControlReturnV2 {
    return ControlReturnV2.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ControlReturnV2>, I>>(object: I): ControlReturnV2 {
    const message = createBaseControlReturnV2();
    message.controlException = (object.controlException !== undefined && object.controlException !== null)
      ? ControlException.fromPartial(object.controlException)
      : undefined;
    message.workerState = object.workerState ?? undefined;
    message.workerMetrics = (object.workerMetrics !== undefined && object.workerMetrics !== null)
      ? WorkerMetrics.fromPartial(object.workerMetrics)
      : undefined;
    message.currentInputTupleInfo =
      (object.currentInputTupleInfo !== undefined && object.currentInputTupleInfo !== null)
        ? CurrentInputTupleInfo.fromPartial(object.currentInputTupleInfo)
        : undefined;
    message.evaluatedValue = (object.evaluatedValue !== undefined && object.evaluatedValue !== null)
      ? EvaluatedValue.fromPartial(object.evaluatedValue)
      : undefined;
    return message;
  },
};

type Builtin = Date | Function | Uint8Array | string | number | boolean | undefined;

export type DeepPartial<T> = T extends Builtin ? T
  : T extends globalThis.Array<infer U> ? globalThis.Array<DeepPartial<U>>
  : T extends ReadonlyArray<infer U> ? ReadonlyArray<DeepPartial<U>>
  : T extends {} ? { [K in keyof T]?: DeepPartial<T[K]> }
  : Partial<T>;

type KeysOfUnion<T> = T extends T ? keyof T : never;
export type Exact<P, I extends P> = P extends Builtin ? P
  : P & { [K in keyof P]: Exact<P[K], I[K]> } & { [K in Exclude<keyof I, KeysOfUnion<P>>]: never };

function isSet(value: any): boolean {
  return value !== null && value !== undefined;
}

export interface MessageFns<T> {
  encode(message: T, writer?: BinaryWriter): BinaryWriter;
  decode(input: BinaryReader | Uint8Array, length?: number): T;
  fromJSON(object: any): T;
  toJSON(message: T): unknown;
  create<I extends Exact<DeepPartial<T>, I>>(base?: I): T;
  fromPartial<I extends Exact<DeepPartial<T>, I>>(object: I): T;
}
