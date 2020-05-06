import { Component, OnInit } from '@angular/core';

import { Field } from '../field';
import { FieldService } from '../field.service';

@Component({
  selector: 'app-fields',
  templateUrl: './fields.component.html',
  styleUrls: ['./fields.component.css']
})
export class FieldsComponent implements OnInit {
    fields: Field[];
    
    constructor(
        private fieldService: FieldService
    ) {}
    
    ngOnInit(): void {
        this.readFields();
    }

    createField(field: string, name: string, description: string, type: string, size: string, required: boolean, order: string) {
        let display_order = Number(order);
        this.fieldService.createField({ field, name, description, type, size, required, display_order } as Field)
            .subscribe(field => {
                // add to the model/view
                if (field) this.fields.push(field); });
    }
    
    readFields(): void {
        this.fieldService.readFields()
            .subscribe(fields => this.fields = fields);
    }

    updateField(field: Field) {
        this.fieldService.updateField(field)
            .subscribe(field => {
                // update the model with the field returned
                this.fields.findIndex(u => { return u.field == field.field; });
            });
    }
    
    deleteField(field: Field) {
        this.fieldService.deleteField(field)
            .subscribe(returnedField => {
                // when removal is successful, returnedField == null
                if (!returnedField) {
                    // remove from the model/view
                    this.fields = this.fields.filter(u => { return u !== field;});
                }});
    }
}
