import { Component, OnInit, ViewChild, AfterViewInit, ElementRef } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { Field } from '../field';
import { Option } from '../option';
import { DttService }  from '../dtt.service';

@Component({
  selector: 'app-field',
  templateUrl: './field.component.html',
  styleUrls: ['./field.component.css']
})
export class FieldComponent implements OnInit {

    field: Field;
    value: string;
    valid = false;
    @ViewChild("input") input: ElementRef; // only works the first time TODO
    
    constructor(private route: ActivatedRoute,
                private dttService: DttService) { }
    
    ngOnInit(): void {
        this.dttService.checkStarted();

        // need to subscribe to URL path changes, because the component will be re-used
        // from field to field 
        this.route.paramMap.pipe(
            switchMap((params: ParamMap) =>
                this.dttService.getField(params.get('field')))
        ).subscribe(field => this.field = field);
    }
    
    ngAfterViewInit(): void {
        this.input.nativeElement.focus();
    }

    saveFieldValue() {
        this.dttService.saveFieldValue(this.field.field, this.value);
        this.value = null;
        this.valid = false;
    }

    validity(input) {
        this.valid = input.validity.valid;
        console.log("input " + input);
        if (!this.valid) {
            input.reportValidity();
        }
    }

}
