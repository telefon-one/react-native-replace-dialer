

# react-native-tele

**UPDATE**: Now is compatible with RN 0.60+ (AndroidX)



## Support
- Currently support for Android.  

## To do

 - [ ] Add ToDo

## Examples

- [Android]Simple example (https://github.com/telefon-one/react-native-replace-dialer/blob/master/examples/) 
- [Google.Play] TODO


## Installation

- [Android](https://github.com/telefon-one/react-native-replace-dialer/blob/master/docs/installation_android.md)

## Usage

```javascript
    import {ReplaceDialer} from 'react-native-replace-dialer';

    let tReplaceDialer = new ReplaceDialer();

    if (!tReplaceDialer.isDefault()) {
      console.log('Is NOT default dialer, try to set.');
      if (tReplaceDialer.setDefault()) {
        console.log('Default dialer sucessfully set.');
      } else {
        console.log('Default dialer NOT set');
      }
    }
    
```

