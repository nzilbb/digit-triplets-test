import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { TextComponent } from './text/text.component';
import { TestComponent } from './test/test.component';
import { FieldComponent } from './field/field.component';
import { SoundCheckComponent } from './sound-check/sound-check.component';

const routes: Routes = [
    { path: 'text/:id', component: TextComponent },
    { path: 'text/:id/:id2', component: TextComponent },
    { path: 'field/:field', component: FieldComponent },
    { path: 'test/:mode', component: TestComponent },
    { path: 'sound-check', component: SoundCheckComponent },
    { path: '**', redirectTo: 'text/introduction' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
