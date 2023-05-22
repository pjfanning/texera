import { JSONSchema7, JSONSchema7Definition } from "json-schema";

export const hideTypes = ["regex", "equals"] as const;
export type HideType = typeof hideTypes[number];

interface AttributeType {
  enum?: string[];
  const?: {
    "$data": string;
  }
}

interface AttributeType1 {
  [key: string]: {
    enum?: string[];
    const?: {
      "$data": string
    }
  }
}

export interface CustomJSONSchema7 extends JSONSchema7 {
  propertyOrder?: number;
  properties?: {
    [key: string]: CustomJSONSchema7 | boolean;
  };
  items?: CustomJSONSchema7 | boolean | JSONSchema7Definition[];

  // new custom properties:
  autofill?: "attributeName" | "attributeNameList";
  autofillAttributeOnPort?: number;
  attributeType?: AttributeType;
  attributeType1?: AttributeType1;

  "enable-presets"?: boolean; // include property in schema of preset

  dependOn?: string;
  toggleHidden?: string[]; // the field names which will be toggle hidden or not by this field.

  hideExpectedValue?: string;
  hideTarget?: string;
  hideType?: HideType;
  hideOnNull?: boolean;

  additionalEnumValue?: string;
}
