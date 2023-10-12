export interface Dataset {
  did: number | undefined;
  name: string;
  is_public: number;
  storage_path: string | undefined;
  description: string;
  creation_time: number | undefined;
}
