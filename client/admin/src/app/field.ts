import { Option } from './option';

export interface Field {
    field: string;
    name: string;
    description: string;
    postscript: string;
    type: string;
    required: boolean;
    display_order: number;
    options: Option[];

    changed: boolean;
}
