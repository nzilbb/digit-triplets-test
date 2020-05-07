import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';

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
    newOption: Option;
    @Output() change = new EventEmitter<Option>();
    
    constructor(
        private fieldService: FieldService
    ) {
        this.newOption = {value : "", description : ""} as Option;
    }

    ngOnInit(): void {
    }

    createOption() {
        if (!this.field.options) this.field.options = [];
        this.field.options.push(this.newOption);
        this.change.emit(this.newOption);
        this.newOption = {value : "", description : ""} as Option;
    }
    
    deleteOption(option: Option) {
        // remove from the model/view
        this.field.options = this.field.options.filter(u => { return u !== option;});
        this.change.emit(null);
    }
    
    onChange(option: Option) {
        this.change.emit(option);
    }

}
