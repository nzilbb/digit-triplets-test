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
    leaving = false;
    reentering = false;
    @ViewChild("input") input: ElementRef; // only works the first time TODO
    
    constructor(private route: ActivatedRoute,
                private dttService: DttService) { }

    ngOnInit(): void {
        console.log("ngOnInit");
        this.dttService.checkStarted();

        // need to subscribe to URL path changes, because the component will be re-used
        // from field to field 
        this.route.paramMap.pipe(
            switchMap((params: ParamMap) =>
                this.dttService.getField(params.get('field')))
        ).subscribe(field => {
            console.log("field " + JSON.stringify(field));
            this.field = field;
            setTimeout(()=>{
                if (this.leaving) {
                    this.reentering = true;
                }
                this.leaving = false;
            }, 750);
        });
    }
    
    ngAfterViewInit(): void {
        console.log("ngAfterViewInit");
        this.input.nativeElement.focus();
        this.valid = !this.field.required;
    }

    saveFieldValue() {
        this.leaving = true;
        console.log("saveFieldValue...");
        this.dttService.saveFieldValue(this.field.field, this.value);
        console.log("saveFieldValue done.");
        this.value = null;
    }

    validity(input) {
        this.valid = input.validity.valid;
        if (!this.valid) {
            input.reportValidity();
        }
    }

}
