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
    changed = false;
    
    constructor(
        private fieldService: FieldService
    ) {}
    
    ngOnInit(): void {
        this.readFields();
    }

    createField(field: string, name: string, description: string, postscript: string, type: string, required: boolean, order: string) {
        let display_order = Number(order);
        this.fieldService.createField({ field, name, description, postscript, type, required, display_order } as Field)
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
                const i = this.fields.findIndex(u => { return u.field == field.field; })
                this.fields[i] = field;
                this.updateChangedFlag();
            });
    }
    
    deleteField(field: Field) {
        this.fieldService.deleteField(field)
            .subscribe(returnedField => {
                // when removal is successful, returnedField == null
                if (!returnedField) {
                    // remove from the model/view
                    this.fields = this.fields.filter(u => { return u !== field;});
                    this.updateChangedFlag();
                }});
    }
    
    onChange(field: Field) {
        field.changed = this.changed = true;        
    }

    updateChangedFields() {
        this.fields
            .filter(f => f.changed)
            .forEach(f => this.updateField(f));
    }

    updateChangedFlag() {
        this.changed = false;
        for (let field of this.fields) {
            if (field.changed) {
                this.changed = true;
                break; // only need to find one
            }
        } // next field
    }
}
