import { Option } from './option';

export interface Field {
    field: string;
    name: string;
    description: string;
    type: string;
    required: boolean;
    postscript: string;
    display_order: number;
    options: Option[];
}
