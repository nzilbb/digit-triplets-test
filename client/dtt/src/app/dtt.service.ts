import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';
import { APP_BASE_HREF, Location } from '@angular/common';

import { Text } from './text';
import { Attribute } from './attribute';
import { Field } from './field';
import { Option } from './option';
import { Instance } from './instance';
import { Result } from './result';
import { MessageService } from './message.service';

@Injectable({
  providedIn: 'root'
})
export class DttService {
    private baseUrl = environment.baseUrl;
    private instance = {} as Instance;
    private fields: Field[];
    private resultTexts: string[];
    private nextMode: string;
    private otherInstanceId: string;

    jsonRequestOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };

    constructor(
        private http: HttpClient,
        private router: Router,
        private messageService: MessageService
    ) {
        this.resultTexts = [];
        this.baseUrl = this.baseUrl || location.href.replace(/\/#.*$/,"");
    }

    getText(id: string): Observable<Text> {
        return this.http.get<Text>(`${this.baseUrl}/text/${id}`)
            .pipe(
                tap((text) => this.log(`fetched text "${text.id}"`)),
                catchError(this.handleError<Text>('getText', `Could not get text "${id}".`))
            );
    }

    getAttribute(attribute: string): Observable<Attribute> {
        return this.http.get<Attribute>(`${this.baseUrl}/attribute/${attribute}`)
            .pipe(
                tap((attribute) => this.log(`fetched attribute "${attribute.attribute}" = "${attribute.value}"`)),
                catchError(this.handleError<Attribute>('getAttribute', `Could not get attribute "${attribute}".`))
            );
    }

    getNextMode(): string {
        return this.nextMode;
    }

    getNumTrials(): number {
        if (!this.instance) return null;
        return this.instance.numTrials;
    }

    getField(f: string): Observable<Field> {
        for (let field of this.fields) {
            if (field.field === f) return of(field);
        } // next field
        return null;
    }

    nextAfterText(id: string): void {
        if (id === "introduction") {
            this.nextField();
        } else {            
            this.router.navigateByUrl(`/test/${this.instance.mode}`);
        } 
    }

    nextAfterSoundCheck(): void {
        this.router.navigateByUrl(`/text/test${this.instance.mode}`);
    }
    
    loadFields(): void {
        this.log("loadFields");

        // get the fields
        this.http.get<Field[]>(`${this.baseUrl}/fields`)
            .pipe(
                catchError(this.handleError<Field[]>('getText', 'Could not get fields.'))
            ).subscribe((fields: Field[]) => {
                this.log('fetched fields');
                this.fields = fields;
                this.instance.nextField = 0;
                this.instance.fields = {};
                this.nextField();
            });
    }

    nextField(): void {
        this.log("next field: " + this.instance.nextField);
        if (this.instance.nextField < this.fields.length) {
            this.router.navigateByUrl('/field/' + this.fields[this.instance.nextField].field);
        } else {
            if (!this.nextMode) { // first test
                this.log("no more fields - sound-check...");
                // sound check before doing the test
                this.router.navigateByUrl(`/sound-check`);
            } else { // already done a test
                this.log(`no more fields - next round, mode ${this.nextMode}...`);
                // go to the next test
                this.start(this.nextMode);
            }
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
                if (this.instance.nextField < this.fields.length
                    && field === this.fields[this.instance.nextField].field) {
                    // move to next field
                    this.instance.nextField++;
                }
                this.nextField();
            });
    }
    
    start(mode: string): void {
        // get an instance ID
        this.log(`start(${mode})`);
        this.http.post<Instance>(`${this.baseUrl}/test`,
                                 { mode : mode, otherInstanceId : this.otherInstanceId },
                                 this.jsonRequestOptions)
            .pipe(
                tap((instance: Instance) => this.info('start', `Created instance: "${instance.id}"`)),
                catchError(this.handleError<Instance>('start',`Could not create instance with mode "${mode}"` ))
            ).subscribe(instance => {
                if (instance) {
                    this.instance = instance;
                    this.instance.trialCount = 0;
                    this.log("new instance: " + JSON.stringify(this.instance));
                    if (this.nextMode == null) { // first test
                        this.loadFields();
                    } else { // second time around
                        this.log(`next test: ${this.instance.mode}`);
                        // go straight to the test
                        this.router.navigateByUrl(`/text/test${this.instance.mode}`);
                    }
                }
            });
    }

    checkStarted(): void {
        if (this.instance == null || this.instance.mode == null) {
            this.router.navigateByUrl('/');
        }
    }

    volumeCheckUrl(): string {
        return `${this.baseUrl}/mp3/dtt/sound-check.mp3`;
    }
    
    getMedia(answer: string): Observable<Blob> {
        this.log(`getMedia ${this.instance.trialCount}/${this.instance.numTrials}`);
        let url = `${this.baseUrl}/test/${this.instance.id}`;
        if (++this.instance.trialCount <= this.instance.numTrials) { // keep going
            this.log(`mediaUrl ${this.baseUrl}/test/${this.instance.id}?a=${answer}`);
            
            if (!answer) {
                url = `${this.baseUrl}/test/${this.instance.id}`;
            } else {
                url = `${this.baseUrl}/test/${this.instance.id}?a=${answer}`;
            }
        } else { // thi test is finished
            this.log(`mediaUrl - no more trials`);
            // first submit the last answer
            this.http.get(`${this.baseUrl}/test/${this.instance.id}?a=${answer}`, {
                responseType : "arraybuffer" })
                .subscribe(_=> { // then get the result
                    this.log(`mediaUrl - submitted last answer`);
                    this.http.get<Result>(`${this.baseUrl}/test/${this.instance.id}/result`)
                        .subscribe(result => { // then get the result
                            this.log(`mediaUrl - result: ${result.textId} - ${result.mode}`);
                            this.resultTexts.push(result.textId);
                            this.otherInstanceId = this.instance.id;
                            this.nextMode = result.mode;
                            this.instance = null;
                            if (this.nextMode != null) {
                                this.start(this.nextMode);
                            } else {
                                this.showResults(this.resultTexts);
                            }
                        });
                });
            url = `${this.baseUrl}/mp3/silence.mp3`
        }
        return this.http.get(url, { responseType: 'blob' })
            .pipe(
                catchError(this.handleError<Blob>('getMedia',`Could not fetch next triplet`)));
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
                            this.resultTexts.push(result.textId);
                            this.otherInstanceId = this.instance.id;
                            this.nextMode = result.mode;
                            this.instance = null;
                            if (this.nextMode != null) {
                                this.start(this.nextMode);
                            } else {
                                this.showResults(this.resultTexts);
                            }
                        });
                });
            return `${this.baseUrl}/mp3/silence.mp3`;
        }
    }

    showResults(textIds: string[]): void {
        let url = "/text";
        for (let id of textIds) url += `/${id}`;
        this.router.navigateByUrl(url);
    }

    handleError<T>(operation = 'operation', message = "ERROR", result?:T) {
        return (error: any): Observable<T> => {
            console.error(error); // TODO something informative for the user?
            if (error.error && error.error.message) message += " - " + error.error.message;
            this.error(operation, message);
            // Let the app keep running by returning an empty result.
            this.messageService.error(message);
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
