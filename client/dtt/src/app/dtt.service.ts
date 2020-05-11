import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { Text } from './text';
import { Field } from './field';
import { Option } from './option';
import { Instance } from './instance';

@Injectable({
  providedIn: 'root'
})
export class DttService {
    private baseUrl = environment.baseUrl;
    private instance = {} as Instance;
    private fields: Field[];

    constructor(
        private http: HttpClient,
        private router: Router
    ) { }

    getText(id: string): Observable<Text> {
        return this.http.get<Text>(`${this.baseUrl}/text/${id}`)
            .pipe(
                tap((text) => this.log(`fetched text "${text.id}"`)),
                catchError(this.handleError<Text>('getText', `Could not get text "${id}".`))
            );
    }

    getField(f: string): Observable<Field> {
        for (let field of this.fields) {
            if (field.field === f) return of(field);
        } // next field
        return null;
    }

    nextAfterText(id: string): void {
        if (id === "introduction") {
            this.router.navigateByUrl('/sound-check');
        }
    }
    
    nextAfterSoundCheck(): void {
        this.log("nextAfterSoundCheck");

        // get the fields
        this.http.get<Field[]>(`${this.baseUrl}/fields`)
            .pipe(
                tap((fields: Field[]) => {
                    this.log('fetched fields');
                    this.fields = fields;
                    this.instance.nextField = 0;
                    this.instance.fields = {};
                    this.nextField();
                }),
                catchError(this.handleError<Field[]>('getText', 'Could not get fields.'))
            ).subscribe();
    }

    nextField(): void {
        console.log("next field: " + this.instance.nextField);
        if (this.instance.nextField < this.fields.length) {
            this.router.navigateByUrl('/field/' + this.fields[this.instance.nextField].field);
        } else {
            this.router.navigateByUrl(`/test/${this.instance.mode}`);
        }
    }

    saveFieldValue(field: string, value: string): void {
        this.log(`${field} = ${value}`);
        this.instance.fields[field] = value;
        if (field === this.fields[this.instance.nextField].field) {
            // move to next field
            this.instance.nextField++;
        }
        this.nextField();
    }
    
    start(mode: string): void {
        this.instance.mode = mode;
        this.router.navigateByUrl('/sound-check');
    }

    checkStarted(): void {
        if (this.instance.mode == null) {
            this.router.navigateByUrl('/');
        }
    }

    volumeCheckUrl(): string {
        return `${this.baseUrl}/mp3/DTT${this.instance.mode}/sound-check.mp3`;
    }


    handleError<T>(operation = 'operation', message = "ERROR", result?:T) {
        return (error: any): Observable<T> => {
            console.error(error);
            if (error.error && error.error.message) message += " - " + error.error.message;
            this.error(operation, message);
            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }
    
    private info(operation: string, message: string) {
        this.log(`${operation} - ${message}`);
    }
    
    private error(operation: string, message: string) {
        console.error(`ERROR: ${operation} - ${message}`);
    }
    
    private log(message: string) {
        console.log(message);
    }

}
