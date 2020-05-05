import { Component, OnInit, Input } from '@angular/core';

import { Field } from '../field';
import { Option } from '../option';
import { FieldService } from '../field.service';

@Component({
  selector: 'app-options',
  templateUrl: './options.component.html',
  styleUrls: ['./options.component.css']
})
export class OptionsComponent implements OnInit {
    @Input() field: Field;
    
    constructor(
        private fieldService: FieldService
    ) { }

    ngOnInit(): void {
    }

    createOption(value: string, description: string) {
        if (!this.field.options) this.field.options = [];
        this.field.options.push({ value, description } as Option); // TODO:
        console.log("options " + this.field.options.length);
    }
    
    deleteOption(option: Option) {
        // remove from the model/view
        this.field.options = this.field.options.filter(u => { return u !== option;});
    }
}
