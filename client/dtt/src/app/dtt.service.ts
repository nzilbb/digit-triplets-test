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
import { Result } from './result';

@Injectable({
  providedIn: 'root'
})
export class DttService {
    private baseUrl = environment.baseUrl;
    private instance = {} as Instance;
    private fields: Field[];
    private nextMode: string;

    jsonRequestOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };


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

    getNextMode(): string {
        return this.nextMode;
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
        } else if (this.nextMode) {
            
            this.router.navigateByUrl(`/sound-check`);
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
        this.http.post<any>(`${this.baseUrl}/test/${this.instance.id}`, {
            field : field, value : ""/*TODO WTF??*/+value }, this.jsonRequestOptions)
            .pipe(
                tap(_ => this.info('field', `Saved: ${field} = "${value}"`)),
                catchError(this.handleError<Instance>('field',`Could not save ${field} = "${value}"` ))
            ).subscribe(_ => {
                if (field === this.fields[this.instance.nextField].field) {
                    // move to next field
                    this.instance.nextField++;
                }
                this.nextField();
            });
    }
    
    start(mode: string): void {
        // get an instance ID
        this.http.post<Instance>(`${this.baseUrl}/test`, { mode : mode }, this.jsonRequestOptions)
            .pipe(
                tap((instance: Instance) => this.info('start', `Created instance: "${instance.id}"`)),
                catchError(this.handleError<Instance>('start',`Could not create instance with mode "${mode}"` ))
            ).subscribe(instance => {
                if (instance) {
                    this.instance = instance;
                    this.instance.trialCount = 0;
                    this.log("new instance: " + JSON.stringify(this.instance));
                    this.router.navigateByUrl('/sound-check');
                    //TODO remove: this.router.navigateByUrl(`/test/${this.instance.mode}`);
                }
            });
    }

    checkStarted(): void {
        if (this.instance.mode == null) {
            this.router.navigateByUrl('/');
        }
    }

    volumeCheckUrl(): string {
        return `${this.baseUrl}/mp3/DTT${this.instance.mode}/sound-check.mp3`;
    }

    mediaUrl(answer: string): string {
        this.log(`mediaUrl ${this.instance.trialCount}/${this.instance.numTrials}`);
        if (++this.instance.trialCount <= this.instance.numTrials) { // keep going
            this.log(`mediaUrl ${this.baseUrl}/test/${this.instance.id}?a=${answer}`);
            if (!answer) {
                return `${this.baseUrl}/test/${this.instance.id}`;
            } else {
                return `${this.baseUrl}/test/${this.instance.id}?a=${answer}`;
            }
        } else { // this test is finished
            this.log(`mediaUrl - no more trials`);
            // first submit the last answer
            this.http.get(`${this.baseUrl}/test/${this.instance.id}?a=${answer}`, {
                responseType : "arraybuffer" })
                .subscribe(_=> { // then get the result
                    this.log(`mediaUrl - submitted last answer`);
                    this.http.get<Result>(`${this.baseUrl}/test/${this.instance.id}/result`)
                        .subscribe(result => { // then get the result
                            this.log(`mediaUrl - result: ${result.textId} - ${result.mode}`);
                            this.router.navigateByUrl(`/text/${result.textId}`);
                            this.instance = null; // TODO link left with right
                            this.nextMode = result.mode;
                        });
                });
            return null;
        }
    }

    handleError<T>(operation = 'operation', message = "ERROR", result?:T) {
        return (error: any): Observable<T> => {
            console.error(error); // TODO something informative for the user?
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
