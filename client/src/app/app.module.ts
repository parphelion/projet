import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserModule, Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FlexLayoutModule } from "@angular/flex-layout";
import { TimeSinceModule } from '@thisissoon/angular-timesince';

import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';

import { AngularMaterialModule } from './angular-material.module';
import { MatDialogModule } from '@angular/material/dialog';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';

import { authInterceptorProviders } from './helpers/auth.interceptor';
import { LoggedInAuthGuard } from './guards/loggedinauthguard.guard';
import { AuthGuard } from './guards/AuthGuard.guard';

import { UploadformService } from './services/uploadform.service';

import { AppComponent } from './app.component';
import { HeaderComponent } from './components/header/header.component';
import { RegisterComponent } from './components/register/register.component';
import { LoginComponent } from './components/login/login.component';
import { HomeComponent } from './components/home/home.component';
import { PostComponent } from './components/post/post.component';
import { UploadComponent } from './components/upload/upload.component';
import { ConfirmationDialogComponent } from './components/shared/confirmation-dialog/confirmation-dialog.component';

@NgModule({
  imports: [
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    FlexLayoutModule,
    AngularMaterialModule,
    MatDialogModule,
    TimeSinceModule
  ],
  declarations: [
    AppComponent,
    HeaderComponent,
    RegisterComponent,
    LoginComponent,
    HomeComponent,
    PostComponent,
    UploadComponent,
    ConfirmationDialogComponent
  ],
  exports: [
    UploadComponent
  ],
  entryComponents: [UploadComponent, ConfirmationDialogComponent],
  providers: [
    authInterceptorProviders, 
    LoggedInAuthGuard, 
    AuthGuard,
    Title,
    UploadformService,
    {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: { appearance: 'outline' }}
  ],
  bootstrap: [AppComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})

export class AppModule { }
