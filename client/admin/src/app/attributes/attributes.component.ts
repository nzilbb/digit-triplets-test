import { Component, OnInit, Input } from '@angular/core';
import { Attribute } from '../attribute';
import { AttributeService } from '../attribute.service';

@Component({
    selector: 'app-attributes',
    templateUrl: './attributes.component.html',
    styleUrls: ['./attributes.component.css']
})
export class AttributesComponent implements OnInit {
    attributes: Attribute[];
    changed = false;
    
    constructor(
        private attributeService: AttributeService
    ) {}
    
    ngOnInit(): void {
        this.readAttributes();
    }
    
    readAttributes(): void {
        this.attributeService.readAttributes()
            .subscribe(attributes => this.attributes = attributes);
    }
    
    updateAttribute(attribute: Attribute) {
        this.attributeService.updateAttribute(attribute)
            .subscribe(attribute => {
                attribute.changed = false; // this may have been passed back unchanged
                // update the model with the attribute returned
                const i = this.attributes.findIndex(u => { return u.attribute == attribute.attribute; });
                if (i >= 0) this.attributes[i] = attribute;
                this.updateChangedFlag();
            });
    }
    
    onChange(attribute: Attribute) {
        attribute.changed = this.changed = true;        
    }

    updateChangedAttributes() {
        this.attributes
            .filter(u => u.changed)
            .forEach(u => this.updateAttribute(u));
    }
    
    updateChangedFlag() {
        this.changed = false;
        for (let attribute of this.attributes) {
            if (attribute.changed) {
                this.changed = true;
                break; // only need to find one
            }
        } // next attribute
    }
}
