import { Option } from './option';

export interface Field {
    field: string;
    name: string;
    description: string;
    type: string;
    size: string;
    required: boolean;
    display_order: number;
    options: Option[];
}
