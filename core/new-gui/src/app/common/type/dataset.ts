export interface Dataset {
  did: number | undefined;
  name: string;
  isPublic: number;
  storagePath: string | undefined;
  description: string;
  creationTime: number | undefined;
}
