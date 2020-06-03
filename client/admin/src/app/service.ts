import { Observable, of } from 'rxjs';
import { HttpHeaders } from '@angular/common/http';
import { MessageService } from './message.service';

export class Service {

    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json;charset=UTF-8' })
    };

    constructor(
        private messageService: MessageService,
        protected baseUrl: string
    ) {}
    
    handleError<T>(operation = 'do that', message = "ERROR", result?:T) {
        return (error: any): Observable<T> => {
            if (error.status == 403) {
                this.error(operation, `Sorry, you don't have permission to ${operation}.`);
            } else {
                console.error(error);
                if (error.error && error.error.message) {
                    message += (message?" - ":"") + error.error.message;
                }
                this.error(operation, message);
            }
            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }

    info(operation: string, message: string) {
        this.messageService.info(message);
        this.log(`${operation} - ${message}`);
    }
    
    error(operation: string, message: string) {
        this.messageService.error(message);
        this.log(`ERROR: ${operation} - ${message}`);
    }
    
    log(message: string) {
        console.log(message);
    }
}
