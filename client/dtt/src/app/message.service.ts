import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
    message: string;

    constructor() { }

    error(message: string): void {
        this.message = message;
    }

}
